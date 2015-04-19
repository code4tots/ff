import java.io.InputStream;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity {

	private FfRuntime.Scope scope;
	private FfRuntime.Function onCreateCallback;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		scope = FfRuntime.declareBuiltins(new FfRuntime.GlobalScope());

        scope.declare("android", createAndroidModule());

        FfRuntime.eval(scope, FfCompiler.parse(Program.CODE));

		if (onCreateCallback != null)
			onCreateCallback.call(new FfRuntime.List());
	}

	private View getRawView(FfRuntime.Dict wrappedView) {
		return (View) ((FfRuntime.Function)wrappedView.get("__getRawView__")).call(new FfRuntime.List());
	}

	private FfRuntime.Dict createAndroidModule() {
		FfRuntime.Dict android = new FfRuntime.Dict();

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "onCreate";
			}

			public Object call(FfRuntime.List args) {
				onCreateCallback = (FfRuntime.Function) args.get(0);
				return onCreateCallback;
			}

		});

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "setView";
			}

			public Object call(FfRuntime.List args) {
				FfRuntime.Dict wrappedView = (FfRuntime.Dict) args.get(0);
				View rawView = getRawView(wrappedView);
				setContentView(rawView);
				return wrappedView;
			}
		});

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "text";
			}

			public Object call(FfRuntime.List args) {
				FfRuntime.Dict tv = new FfRuntime.Dict();
				final TextView rawView = new TextView(MainActivity.this);
				rawView.setText((String) args.get(0));
				tv.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "__getRawView__";
					}

					public Object call(FfRuntime.List args) {
						return rawView;
					}
				});
				tv.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "setText";
					}

					public Object call(FfRuntime.List	args) {
						rawView.setText((String) args.get(0));
						return args.get(0);
					}
				});
				return tv;
			}
		});

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "button";
			}

			public Object call(FfRuntime.List args) {
				FfRuntime.Dict bv = new FfRuntime.Dict();
				final Button rawView = new Button(MainActivity.this);
				rawView.setText((String) args.get(0));
				if (args.size() > 1) {
					final FfRuntime.Function callback = (FfRuntime.Function) args.get(1);
					rawView.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							callback.call(new FfRuntime.List());
						}
					});
				}
				bv.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "__getRawView__";
					}

					public Object call(FfRuntime.List args) {
						return rawView;
					}
				});
				bv.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "setText";
					}

					public Object call(FfRuntime.List	args) {
						rawView.setText((String) args.get(0));
						return args.get(0);
					}
				});
				bv.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "onClick";
					}

					public Object call(FfRuntime.List args) {
						final FfRuntime.Function callback = (FfRuntime.Function) args.get(0);
						rawView.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								callback.call(new FfRuntime.List());
							}
						});
						return callback;
					}

				});
				return bv;
			}
		});

		android.putBuiltin(new LayoutBuiltin() {

			public String getName() {
				return "vertical";
			}

			public int getOrientation() {
				return LinearLayout.VERTICAL;
			}
		});

		android.putBuiltin(new LayoutBuiltin() {

			public String getName() {
				return "horizontal";
			}

			public int getOrientation() {
				return LinearLayout.HORIZONTAL;
			}

		});

		return android;
	}

	abstract private class LayoutBuiltin extends FfRuntime.Builtin {

		abstract public int getOrientation();

		public Object call(FfRuntime.List args) {
			FfRuntime.Dict lv = new FfRuntime.Dict();
			final LinearLayout rawView = new LinearLayout(MainActivity.this);

			rawView.setOrientation(getOrientation());

			for (int i = 0; i < args.size(); i++)
				rawView.addView(getRawView((FfRuntime.Dict) args.get(i)));

			lv.putBuiltin(new FfRuntime.Builtin() {

				public String getName() {
					return "__getRawView__";
				}

				public Object call(FfRuntime.List args) {
					return rawView;
				}
			});

			lv.putBuiltin(new FfRuntime.Builtin() {

				public String getName() {
					return "add";
				}

				public Object call(FfRuntime.List args) {
					FfRuntime.Dict wrappedView = (FfRuntime.Dict) args.get(0);
					View iView = getRawView(wrappedView);
					rawView.addView(iView);
					return wrappedView;
				}

			});
			return lv;
		}
	}
}
