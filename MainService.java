import java.io.IOException;

import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.AudioManager;

public class MainService extends Service {

	private final IBinder binder = new Binder();
	private final MediaPlayer mediaPlayer = new MediaPlayer();

	public class Binder extends android.os.Binder {
		MainService getService() {
			return MainService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public boolean playAudio(String url) {
		mediaPlayer.reset();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(url);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				mediaPlayer.start();
			}
		});
		mediaPlayer.prepareAsync();
		return true;
	}

	public boolean pauseAudio() {
		try{
			mediaPlayer.pause();
			return true;
		}
		catch (IllegalStateException e) {
			return false;
		}
	}

	public boolean resumeAudio() {
		try {
			mediaPlayer.start();
			return true;
		}
		catch (IllegalStateException e) {
			return false;
		}
	}

}
