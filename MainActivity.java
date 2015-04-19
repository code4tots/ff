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

public class MainActivity extends Activity {
	private FfRuntime.Scope scope;
	private FfRuntime.Function onCreateCallback;
	private MainService service;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((MainService.Binder) binder).getService();
			if (onCreateCallback != null)
				onCreateCallback.call(new FfRuntime.List());
		}
		@Override
		public void onServiceDisconnected(ComponentName className) {
			service = null;
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(this, MainService.class), connection, Context.BIND_AUTO_CREATE);
		scope = FfRuntime.declareBuiltins(new FfRuntime.GlobalScope());
        scope.declare("android", createAndroidModule());
        FfRuntime.eval(scope, FfCompiler.parse(Program.CODE));
	}
	public MainService getService() { return service; }
	private FfRuntime.Dict createAndroidModule() {
		FfRuntime.Dict android = new FfRuntime.Dict();
		new AndroidWidgetPlugin(this, android);
		new AndroidAudioPlugin(this, android);
		android.putBuiltin(new FfRuntime.Builtin() {
			public String getName() { return "onCreate"; }
			public Object call(FfRuntime.List args) {
				onCreateCallback = (FfRuntime.Function) args.get(0);
				return onCreateCallback;
			}
		});
		return android;
	}
}
