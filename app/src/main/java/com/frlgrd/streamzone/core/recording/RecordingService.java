package com.frlgrd.streamzone.core.recording;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import com.frlgrd.streamzone.App;
import com.frlgrd.streamzone.core.event.MediaProjectionInfosHolder;
import com.frlgrd.streamzone.core.event.RecordServiceReady;
import com.frlgrd.streamzone.core.event.StartRecording;
import com.frlgrd.streamzone.core.event.StopRecording;
import com.squareup.otto.Subscribe;

import java.io.IOException;

public class RecordingService extends Service {

	private static final String TAG = "RecordingService";
	private static final int DISPLAY_WIDTH = 720;
	private static final int DISPLAY_HEIGHT = 1280;
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
	public static boolean ready = false;

	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 90);
		ORIENTATIONS.append(Surface.ROTATION_90, 0);
		ORIENTATIONS.append(Surface.ROTATION_180, 270);
		ORIENTATIONS.append(Surface.ROTATION_270, 180);
	}

	private Intent launcherIntent = null;
	private int resultCodeFromActivityResult = 0;
	private int screenDensity;
	private MediaProjectionManager projectionManager;
	private MediaProjection mediaProjection;
	private VirtualDisplay virtualDisplay;
	private MediaProjectionCallback mediaProjectionCallback;
	private MediaRecorder mediaRecorder;
	private WindowManager windowService;

	@Subscribe
	public void onMediaProjectInfoReceived(MediaProjectionInfosHolder holder) {
		launcherIntent = holder.getIntent();
		resultCodeFromActivityResult = holder.getResultCode();
		ready = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		windowService = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
		retrieveDisplayMetrics();
		mediaRecorder = new MediaRecorder();
		App.BUS.register(this);
		App.BUS.post(new RecordServiceReady());
		return START_REDELIVER_INTENT;
	}

	private void retrieveDisplayMetrics() {
		DisplayMetrics metrics = new DisplayMetrics();
		windowService.getDefaultDisplay().getMetrics(metrics);
		screenDensity = metrics.densityDpi;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void stopScreenSharing() {
		if (virtualDisplay == null) {
			return;
		}
		virtualDisplay.release();
		destroyMediaProjection();
	}

	@Subscribe
	public void start(StartRecording startRecording) {
		initRecorder();
		mediaProjectionCallback = new MediaProjectionCallback();
		mediaProjection = projectionManager.getMediaProjection(resultCodeFromActivityResult, launcherIntent);
		mediaProjection.registerCallback(mediaProjectionCallback, null);
		virtualDisplay = createVirtualDisplay();
		mediaRecorder.start();
	}

	@Subscribe
	public void stop(StopRecording stopRecording) {
		mediaRecorder.stop();
		mediaRecorder.reset();
		Log.v(TAG, "Stopping Recording");
		stopScreenSharing();
	}

	private VirtualDisplay createVirtualDisplay() {
		return mediaProjection.createVirtualDisplay("RecordingActivity",
				DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
				mediaRecorder.getSurface(), null, null);
	}

	private void initRecorder() {
		try {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecorder.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/video.mp4");
			mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setVideoEncodingBitRate(512 * 1000);
			mediaRecorder.setVideoFrameRate(30);
			int orientation = ORIENTATIONS.get(windowService.getDefaultDisplay().getRotation() + 90);
			mediaRecorder.setOrientationHint(orientation);
			mediaRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		destroyMediaProjection();
		App.BUS.unregister(this);
		ready = false;
	}

	private void destroyMediaProjection() {
		if (mediaProjection != null) {
			mediaProjection.unregisterCallback(mediaProjectionCallback);
			mediaProjection.stop();
			mediaProjection = null;
		}
		Log.i(TAG, "MediaProjection Stopped");
	}

	private class MediaProjectionCallback extends MediaProjection.Callback {
		@Override
		public void onStop() {
			try {
				mediaRecorder.stop();
				mediaRecorder.reset();
			} catch (IllegalStateException ignored) {
			}
			Log.v(TAG, "Recording Stopped");
			mediaProjection = null;
			stopScreenSharing();
		}
	}
}
