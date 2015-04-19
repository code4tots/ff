public class AndroidAudioPlugin {

	public AndroidAudioPlugin(final MainActivity activity, FfRuntime.Dict android) {

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "playAudio";
			}

			public Object call(FfRuntime.List args) {
				return activity.getService().playAudio((String) args.get(0)) ? "1" : "";
			}

		});

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "pauseAudio";
			}

			public Object call(FfRuntime.List args) {
				return activity.getService().pauseAudio() ? "1" : "";
			}

		});

		android.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "resumeAudio";
			}

			public Object call(FfRuntime.List args) {
				return activity.getService().resumeAudio() ? "1" : "";
			}

		});

	}

}