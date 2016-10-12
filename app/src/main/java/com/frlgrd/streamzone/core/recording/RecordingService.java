package com.frlgrd.streamzone.core.recording;

import android.app.Service;
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

import com.frlgrd.streamzone.core.event.Otto;
import com.frlgrd.streamzone.core.event.PrepareRecordingEvent;
import com.frlgrd.streamzone.core.event.StopRecordingEvent;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;

import java.io.IOException;

@SuppressWarnings("unused")
@EService
public class RecordingService extends Service {

	private static final String TAG = "RecordingService";
	private static final int DISPLAY_WIDTH = 720;
	private static final int DISPLAY_HEIGHT = 1280;
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 90);
		ORIENTATIONS.append(Surface.ROTATION_90, 0);
		ORIENTATIONS.append(Surface.ROTATION_180, 270);
		ORIENTATIONS.append(Surface.ROTATION_270, 180);
	}

	@Bean RecordingHelper recordingHelper;
	@Bean Otto otto;
	@Bean RecordingNotificationHelper recordingNotificationHelper;
	@SystemService WindowManager windowManager;
	@SystemService MediaProjectionManager mediaProjectionManager;

	private int screenDensity;
	private MediaProjection mediaProjection;
	private VirtualDisplay virtualDisplay;
	private MediaProjectionCallback mediaProjectionCallback;
	private MediaRecorder mediaRecorder;

	@Override public int onStartCommand(Intent intent, int flags, int startId) {
		otto.register(this);
		retrieveDisplayMetrics();
		mediaRecorder = new MediaRecorder();
		recordingHelper.ready();
		return START_REDELIVER_INTENT;
	}

	private void retrieveDisplayMetrics() {
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(metrics);
		screenDensity = metrics.densityDpi;
	}

	@Nullable @Override public IBinder onBind(Intent intent) {
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
	public void prepare(PrepareRecordingEvent prepareRecordingEvent) {
		mediaProjection = mediaProjectionManager.getMediaProjection(prepareRecordingEvent.getResultCode(), prepareRecordingEvent.getIntent());
		start();
	}

	private void start() {
		if (recordingHelper.isRecording()) return;
		recordingHelper.startRecording();
		initRecorder();
		mediaProjectionCallback = new MediaProjectionCallback();
		mediaProjection.registerCallback(mediaProjectionCallback, null);
		virtualDisplay = createVirtualDisplay();
		mediaRecorder.start();
	}

	@Subscribe public void stop(StopRecordingEvent stopRecordingEvent) {
		stop();
	}

	private void stop() {
		if (!recordingHelper.isRecording()) return;
		recordingHelper.stopRecording();
		mediaRecorder.stop();
		mediaRecorder.reset();
		Log.v(TAG, "Stopping Recording");
		stopScreenSharing();
	}

	private VirtualDisplay createVirtualDisplay() {
		return mediaProjection.createVirtualDisplay(getPackageName(),
				DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
				mediaRecorder.getSurface(), null, null);
	}

	private void initRecorder() {
		try {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecorder.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/stream.mp4");
			mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setVideoEncodingBitRate(512 * 1000);
			mediaRecorder.setVideoFrameRate(30);
			int orientation = ORIENTATIONS.get(windowManager.getDefaultDisplay().getRotation() + 90);
			mediaRecorder.setOrientationHint(orientation);
			mediaRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override public void onDestroy() {
		super.onDestroy();
		destroyMediaProjection();
		otto.unregister(this);
		recordingHelper.recorderServiceKilled();
	}

	@Receiver(actions = RecordingHelper.ACTION_RECORD_START)
	void startRequestFromNotification() {
		start();
	}

	@Receiver(actions = RecordingHelper.ACTION_RECORD_STOP)
	void stopRequestFromNotification() {
		stop();
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
		@Override public void onStop() {
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
