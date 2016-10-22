package com.frlgrd.streamzone.core.recording;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.WindowManager;

import com.frlgrd.streamzone.core.streaming.StreamManager;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;

import java.io.IOException;

import rx.Observable;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

@EBean(scope = EBean.Scope.Singleton)
public class RecordingManager {

	private static final int DISPLAY_WIDTH = 720;
	private static final int DISPLAY_HEIGHT = 1280;
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

	@SystemService MediaProjectionManager mediaProjectionManager;
	@SystemService WindowManager windowManager;
	@RootContext Context context;
	@Bean StreamManager streamManager;
	private MediaProjection mediaProjection;
	private int screenDensity;
	private VirtualDisplay virtualDisplay;
	private MediaProjectionCallback mediaProjectionCallback;
	private MediaRecorder mediaRecorder;
	private PublishSubject<Boolean> recordingPermissionPublishSubject;

	private boolean isRecording = false;

	public Observable<Boolean> requestPermissions() {
		return Observable.zip(hasSystemPermission(), hasRecordingPermission(), new Func2<Boolean, Boolean, Boolean>() {
			@Override public Boolean call(Boolean systemPermissionGranted, Boolean recordingPermissionGranted) {
				recordingPermissionPublishSubject.onCompleted();
				return systemPermissionGranted && recordingPermissionGranted;
			}
		});
	}

	private Observable<Boolean> hasRecordingPermission() {
		recordingPermissionPublishSubject = PublishSubject.create();
		HiddenRecordingActivity_.intent(context).start().withAnimation(0, 0);
		return recordingPermissionPublishSubject.asObservable();
	}

	void onStreamPermissionGranted(Intent intent) {
		mediaProjectionCallback = new MediaProjectionCallback();
		mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, intent);
		recordingPermissionPublishSubject.onNext(true);
	}

	@UiThread
	void shareScreen() {
		virtualDisplay = createVirtualDisplay();
		mediaRecorder.start();
		isRecording = true;
	}

	void onStreamPermissionDenied() {
		recordingPermissionPublishSubject.onNext(false);
	}

	private Observable<Boolean> hasSystemPermission() {
		return RxPermissions.getInstance(context).request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	public void start() {
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(metrics);
		screenDensity = metrics.densityDpi;
		mediaRecorder = new MediaRecorder();
		mediaProjection.registerCallback(mediaProjectionCallback, null);
		initRecorder();
	}

	private VirtualDisplay createVirtualDisplay() {
		return mediaProjection.createVirtualDisplay("stream",
				DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
				mediaRecorder.getSurface(), null, null);
	}

	@Background
	void initRecorder() {
		try {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecorder.setOutputFile(streamManager.getParcelFileDescriptor().getFileDescriptor());
			mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setVideoEncodingBitRate(512 * 1000);
			mediaRecorder.setVideoFrameRate(30);
			int rotation = windowManager.getDefaultDisplay().getRotation();
			int orientation = ORIENTATIONS.get(rotation + 90);
			mediaRecorder.setOrientationHint(orientation);
			mediaRecorder.prepare();
			shareScreen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (virtualDisplay == null) {
			return;
		}
		virtualDisplay.release();
		destroyMediaProjection();
	}

	private void destroyMediaProjection() {
		if (mediaProjection != null) {
			mediaProjection.stop();
		}
	}

	public boolean isRecording() {
		return isRecording;
	}

	private class MediaProjectionCallback extends MediaProjection.Callback {
		@Override
		public void onStop() {
			if (isRecording) {
				isRecording = false;
				mediaRecorder.stop();
				mediaRecorder.reset();
			}
			mediaProjection.unregisterCallback(mediaProjectionCallback);
			mediaProjection = null;
			stop();
		}
	}
}
