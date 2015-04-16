import java.util.HashMap;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.IOException;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class FfCompiler {

	public static void main(String[] args) throws Exception {
		FfRuntime.Dict ast = parse(new ANTLRInputStream(System.in));

		if (ast == null)
			System.exit(1);

		System.out.println(ast);
	}

	public static FfRuntime.Dict parse(String string) {
		return parse(new ANTLRInputStream(string));
	}

	public static FfRuntime.Dict parse(InputStream inputStream) {
		try {
			return parse(new ANTLRInputStream(inputStream));
		}
		catch (IOException e) {
			return null;
		}
	}

	public static FfRuntime.Dict parse(ANTLRInputStream inputStream) {
		FfLexer lexer = new FfLexer(inputStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		FfParser parser = new FfParser(tokens);
		Listener listener = new Listener();
		ParserRuleContext tree = parser.start();

		if (parser.getNumberOfSyntaxErrors() > 0)
			return null;

		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(listener, tree);
		return listener.getResult();
	}

	public static class Listener extends FfBaseListener {

		public static String NAME_LIST = "__list__";
		public static String NAME_DICT = "__dict__";
		public static String NAME_GET = "__get__";
		public static String NAME_SET = "__set__";

		private FfRuntime.Dict result;
		private ArrayList<FfRuntime.List> stack;

		public FfRuntime.Dict getResult() {
			return result;
		}

		private FfRuntime.List getLastList() {
			return stack.get(stack.size() - 1);
		}

		private void push(Object value) {
			getLastList().add(value);
		}

		private Object pop() {
			return getLastList().remove(getLastList().size() - 1);
		}

		private void pushStack() {
			stack.add(new FfRuntime.List());
		}

		private FfRuntime.List popStack() {
			return stack.remove(stack.size() - 1);
		}

		public static FfRuntime.Dict makeBlock(FfRuntime.List stmts) {
			return new FfRuntime.Dict("type", "block", "stmts", stmts);
		}

		public static FfRuntime.Dict makeScope(Object body) {
			return new FfRuntime.Dict("type", "scope", "body", body);
		}

		public static FfRuntime.Dict makeIf(Object cond, Object a, Object b) {
			return new FfRuntime.Dict("type", "if", "cond", cond, "a", a, "b", b);
		}

		public static FfRuntime.Dict makeWhile(Object cond, Object body) {
			return new FfRuntime.Dict("type", "while", "cond", cond, "body", body);
		}

		public static FfRuntime.Dict makeStr(String value) {
			return new FfRuntime.Dict("type", "str", "value", value);
		}

		public static FfRuntime.Dict makeName(String value) {
			return new FfRuntime.Dict("type", "name", "value", value);
		}

		public static FfRuntime.Dict makeCall(Object f, FfRuntime.List args) {
			return new FfRuntime.Dict("type", "call", "f", f, "args", args);
		}

		public static FfRuntime.Dict makeDecl(String name, Object value) {
			return new FfRuntime.Dict("type", "decl", "name", name, "value", value);
		}

		public static FfRuntime.Dict makeAssign(String name, Object value) {
			return new FfRuntime.Dict("type", "assign", "name", name, "value", value);
		}

		public static FfRuntime.Dict makeLambda(FfRuntime.List names, Object body) {
			return new FfRuntime.Dict("type", "lambda", "names", names, "body", body);
		}

		@Override
		public void enterStart(FfParser.StartContext ctx) {
			stack = new ArrayList<FfRuntime.List>();
			pushStack();
		}

		@Override
		public void exitStart(FfParser.StartContext ctx) {
			result = makeBlock(popStack());
		}

		@Override
		public void enterB(FfParser.BContext ctx) {
			pushStack();
		}

		@Override
		public void exitB(FfParser.BContext ctx) {
			push(makeScope(makeBlock(popStack())));
		}

		@Override
		public void exitIfElse(FfParser.IfElseContext ctx) {
			push(makeIf(pop(), pop(), pop()));
		}

		@Override
		public void exitIf_(FfParser.If_Context ctx) {
			push(makeIf(pop(), pop(), makeStr("")));
		}

		@Override
		public void exitWhile_(FfParser.While_Context ctx) {
			push(makeWhile(pop(), pop()));
		}

		public static String processStringFragment(String string) {

			// raw string
			if (string.charAt(0) == 'r') {
				return string.substring(2, string.length() - 1);
			}

			// normal string with escapes
			else if (string.charAt(0) == '"' || string.charAt(0) == '\'') {
				StringBuilder sb = new StringBuilder();
				for (int i = 1; i < string.length() - 1; i++) {
					if (string.charAt(i) == '\\') {
						i++;
						if (string.charAt(i) == '\\') {
							sb.append('\\');
						}
						else if (string.charAt(i) == 'n') {
							sb.append('\n');
						}
					}
					else {
						sb.append(string.charAt(i));
					}
				}
				return sb.toString();
			}

			// number string.
			else {
				return string;
			}

		}

		@Override
		public void exitStr(FfParser.StrContext ctx) {
			StringBuilder sb = new StringBuilder();
			int n = ctx.STR().size();
			for (int i = 0; i < n; i++) {
				sb.append(processStringFragment(ctx.STR(i).getText()));
			}
			push(makeStr(sb.toString()));
		}

		@Override
		public void exitName(FfParser.NameContext ctx) {
			push(makeName(ctx.NAME().getText()));
		}

		@Override
		public void enterList(FfParser.ListContext ctx) {
			pushStack();
		}

		@Override
		public void exitList(FfParser.ListContext ctx) {
			push(makeCall(makeName(NAME_LIST), popStack()));
		}

		@Override
		public void enterDict(FfParser.DictContext ctx) {
			pushStack();
		}

		@Override
		public void exitDict(FfParser.DictContext ctx) {
			push(makeCall(makeName(NAME_DICT), popStack()));
		}

		@Override
		public void exitAttr(FfParser.AttrContext ctx) {
			push(makeCall(makeName(NAME_GET), new FfRuntime.List(pop(), makeStr(ctx.NAME().getText()))));
		}

		@Override
		public void exitGetItem(FfParser.GetItemContext ctx) {
			Object attr = pop();
			Object x = pop();
			push(makeCall(makeName(NAME_GET), new FfRuntime.List(x, attr)));
		}

		@Override
		public void enterCall(FfParser.CallContext ctx) {
			pushStack();
		}

		@Override
		public void exitCall(FfParser.CallContext ctx) {
			FfRuntime.List args = popStack();
			Object f = args.remove(0);
			push(makeCall(f, args));
		}

		@Override
		public void exitDecl(FfParser.DeclContext ctx) {
			push(makeDecl(ctx.NAME().getText(), pop()));
		}

		@Override
		public void exitAssign(FfParser.AssignContext ctx) {
			push(makeAssign(ctx.NAME().getText(), pop()));
		}

		@Override
		public void exitAttrAssign(FfParser.AttrAssignContext ctx) {
			Object v = pop();
			Object x = pop();
			push(makeCall(makeName(NAME_SET), new FfRuntime.List(x, makeStr(ctx.NAME().getText()), v)));
		}

		@Override
		public void exitSetItem(FfParser.SetItemContext ctx) {
			Object v = pop();
			Object a = pop();
			Object x = pop();
			push(makeCall(makeName(NAME_SET), new FfRuntime.List(x, a, v)));
		}

		@Override
		public void exitLambda(FfParser.LambdaContext ctx) {
			FfRuntime.List names = new FfRuntime.List();
			int n = ctx.NAME().size();
			for (int i = 0; i < n; i++)
				names.add(ctx.NAME(i).getText());
			push(makeLambda(names, pop()));
		}
	}
}
