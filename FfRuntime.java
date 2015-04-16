import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings({"serial"})
public class FfRuntime {

	/// eval

	public static Object eval(Scope scope, Object d) {
		return eval(scope, (Dict) d);
	}

	public static Object eval(Scope scope, Dict d) {
		String type = (String) d.get("type");
		if (type.equals("block")) {
			List stmts = (List) d.get("stmts");
			Object last = "";
			for (int i = 0; i < stmts.size(); i++)
				last = eval(scope, stmts.get(i));
			return last;
		}
		else if (type.equals("scope"))
			return eval(new LocalScope(scope), d.get("body"));
		else if (type.equals("if"))
			return eval(scope, d.get(truthy(eval(scope, d.get("cond"))) ? "a" : "b"));
		else if (type.equals("while")) {
			Object last = "";
			while (truthy(eval(scope, d.get("cond"))))
				last = eval(scope, d.get("body"));
			return last;
		}
		else if (type.equals("str"))
			return d.get("value");
		else if (type.equals("name"))
			return scope.get((String) d.get("value"));
		else if (type.equals("call")) {
			Function f = (Function) eval(scope, d.get("f"));
			List argDisps = (List) d.get("args");
			List args = new List();
			for (int i = 0; i < argDisps.size(); i++)
				args.add(eval(scope, argDisps.get(i)));
			return f.call(args);
		}
		else if (type.equals("decl"))
			return scope.declare((String) d.get("name"), eval(scope, d.get("value")));
		else if (type.equals("assign"))
			return scope.assign((String) d.get("name"), eval(scope, d.get("value")));
		else if (type.equals("lambda"))
			return new Lambda(scope, (List) d.get("names"), (Dict) d.get("body"));
		throw new Error("Unrecognized eval type '" + type + "'");
	}

	/// Wrappers

	public static boolean truthy(Object x) {
		if (x instanceof String) return !x.equals("");
		if (x instanceof List) return ((List) x).size() != 0;
		if (x instanceof Dict) return ((Dict) x).size() != 0;
		if (x instanceof Function) return true;
		throw new Error("Unrecognized java type " + x.getClass().toString());
	}

	public static class List extends ArrayList<Object> {
		public List(Object... args) {
			for (int i = 0; i < args.length; i++)
				add(args[i]);
		}
	}

	public static class Dict extends HashMap<Object, Object> {
		public Dict(Object... args) {
			for (int i = 0; i < args.length; i += 2)
				put(args[i], args[i+1]);
		}

		public Object putBuiltin(Builtin builtin) {
			return put(builtin.getName(), builtin);
		}
	}

	abstract public static class Function {
		abstract public Object call(List args);
	}

	abstract public static class Builtin extends Function {
		abstract public String getName();

		public String toString() {
			return "<builtin " + getName() + ">";
		}
	}

	public static class Lambda extends Function {
		private List names;
		private Dict body;
		private Scope scope;
		public Lambda(Scope scope, List names, Dict body) {
			this.scope = scope;
			this.names = names;
			this.body = body;
		}
		public Object call(List args) {
			Scope scope = new LocalScope(this.scope);
			for (int i = 0; i < names.size(); i++)
				scope.declare((String) names.get(i), args.get(i));
			List vargs = new List();
			for (int i = names.size(); i < args.size(); i++)
				vargs.add(args.get(i));
			scope.declare("__args__", vargs);
			return eval(scope, body);
		}
		public String toString() {
			return "<lambda>";
		}
	}

	/// Scope

	abstract public static class Scope {
		protected Map<String, Object> table = new HashMap<String, Object>();

		abstract public Object get(String key);
		abstract public Object assign(String key, Object val);

		public Object declare(String key, Object val) {
			if (table.containsKey(key))
				throw new Error(key + " is already declared in current scope");
			table.put(key, val);
			return val;
		}

		public Object declareBuiltin(Builtin builtin) {
			return declare(builtin.getName(), builtin);
		}
	}

	public static class GlobalScope extends Scope {

		public Object get(String key) {
			if (!table.containsKey(key))
				throw new Error(key + " not found");
			return table.get(key);
		}

		public Object assign(String key, Object val) {
			if (!table.containsKey(key))
				throw new Error(key + " not found");
			table.put(key, val);
			return val;
		}
	}

	public static class LocalScope extends Scope {
		private Scope parent;

		public LocalScope(Scope p) {
			parent = p;
		}

		public Object get(String key) {
			return table.containsKey(key) ? table.get(key) : parent.get(key);
		}

		public Object assign(String key, Object val) {
			if (table.containsKey(key))  table.put(key, val);
			else                         parent.assign(key, val);
			return val;
		}
	}

	public static Scope declareBuiltins(Scope scope) {
	
		scope.declareBuiltin(new Builtin() {

			public String getName() {
				return "print";
			}

			public Object call(List args) {
				if (args.size() > 0) {
                    System.out.print(args.get(0));
                    for (int i = 1; i < args.size(); i++) {
                        System.out.print(" ");
                        System.out.print(args.get(i));
                    }
                }
                System.out.println();
                return args.get(args.size() - 1);
			}
		});

		scope.declareBuiltin(new Builtin() {

			public String getName() {
				return "__dict__";
			}

			public Object call(List args) {
				Dict d = new Dict();
				for (int i = 0; i < args.size(); i += 2)
					d.put(args.get(i), args.get(i+1));
				return d;
			}
		});

		scope.declareBuiltin(new Builtin() {

			public String getName() {
				return "__get__";
			}

			public Object call(List args) {
				if (args.size() != 2)
					throw new Error(args.toString());
				Dict d = (Dict) args.get(0);
				String n = (String) args.get(1);
				return d.get(n);
			}
		});

		return scope;
	}
}
