import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.IBinder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AndroidWidgetPlugin {

	final MainActivity activity;

	public AndroidWidgetPlugin(final MainActivity activity, FfRuntime.Dict android) {
		this.activity = activity;

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "setView";
			}

			public Object call(FfRuntime.List args) {
				FfRuntime.Dict wrappedView = (FfRuntime.Dict) args.get(0);
				View rawView = AndroidWidgetPlugin.getRawView(wrappedView);
				activity.setContentView(rawView);
				return wrappedView;
			}
		});

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "text";
			}

			public Object call(FfRuntime.List args) {
				FfRuntime.Dict v = new FfRuntime.Dict();
				final TextView rv = new TextView(activity);
				rv.setText((String) args.get(0));
				v.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "__getRawView__";
					}

					public Object call(FfRuntime.List args) {
						return rv;
					}
				});
				v.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "setText";
					}

					public Object call(FfRuntime.List args) {
						rv.setText((String) args.get(0));
						return args.get(0);
					}
				});
				v.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "getText";
					}

					public Object call(FfRuntime.List args) {
						return rv.getText().toString();
					}
				});
				return v;
			}
		});

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "button";
			}

			public Object call(FfRuntime.List args) {
				FfRuntime.Dict v = new FfRuntime.Dict();
				final Button rv = new Button(activity);
				rv.setText((String) args.get(0));
				if (args.size() > 1) {
					final FfRuntime.Function callback = (FfRuntime.Function) args.get(1);
					rv.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							callback.call(new FfRuntime.List());
						}
					});
				}
				v.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "__getRawView__";
					}

					public Object call(FfRuntime.List args) {
						return rv;
					}
				});
				v.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "setText";
					}

					public Object call(FfRuntime.List args) {
						rv.setText((String) args.get(0));
						return args.get(0);
					}
				});
				v.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "getText";
					}

					public Object call(FfRuntime.List args) {
						return rv.getText().toString();
					}
				});
				v.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "onClick";
					}

					public Object call(FfRuntime.List args) {
						final FfRuntime.Function callback = (FfRuntime.Function) args.get(0);
						rv.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								callback.call(new FfRuntime.List());
							}
						});
						return callback;
					}

				});
				return v;
			}
		});

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "view";
			}

			public Object call(FfRuntime.List args) {
				FfRuntime.Dict v = new FfRuntime.Dict();

				final View rv = new View(activity) {
					public void onDraw(Canvas rc) {
						FfRuntime.Dict c = new FfRuntime.Dict();
					}
				};

				v.putBuiltin(new FfRuntime.Builtin() {

					public String getName() {
						return "__getRawView__";
					}

					public Object call(FfRuntime.List args) {
						return rv;
					}
				});
				return v;
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

	}

	public static View getRawView(FfRuntime.Dict wrappedView) {
		return (View) ((FfRuntime.Function)wrappedView.get("__getRawView__")).call(new FfRuntime.List());
	}

	abstract private class LayoutBuiltin extends FfRuntime.Builtin {

		abstract public int getOrientation();

		public Object call(FfRuntime.List args) {
			FfRuntime.Dict v = new FfRuntime.Dict();
			final LinearLayout rv = new LinearLayout(activity);

			rv.setOrientation(getOrientation());

			for (int i = 0; i < args.size(); i++)
				rv.addView(getRawView((FfRuntime.Dict) args.get(i)));

			v.putBuiltin(new FfRuntime.Builtin() {

				public String getName() {
					return "__getRawView__";
				}

				public Object call(FfRuntime.List args) {
					return rv;
				}
			});

			v.putBuiltin(new FfRuntime.Builtin() {

				public String getName() {
					return "add";
				}

				public Object call(FfRuntime.List args) {
					FfRuntime.Dict wrappedView = (FfRuntime.Dict) args.get(0);
					View iView = getRawView(wrappedView);
					rv.addView(iView);
					return wrappedView;
				}

			});
			return v;
		}
	}
}