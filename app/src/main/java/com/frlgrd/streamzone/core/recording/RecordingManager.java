package com.frlgrd.streamzone.core.recording;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;

import rx.Observable;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

@EBean(scope = EBean.Scope.Singleton)
public class RecordingManager {

	@SystemService MediaProjectionManager mediaProjectionManager;
	@RootContext Context context;

	private MediaProjection mediaProjection;

	private PublishSubject<Boolean> recordingPermissionPublishSubject;

	public Observable<Boolean> requestPermissions() {
		return Observable.zip(hasSystemPermission(), hasRecordingPermission(), new Func2<Boolean, Boolean, Boolean>() {
			@Override public Boolean call(Boolean systemPermissionGranted, Boolean recordingPermissionGranted) {
				return systemPermissionGranted && recordingPermissionGranted;
			}
		});
	}

	private Observable<Boolean> hasRecordingPermission() {
		recordingPermissionPublishSubject = PublishSubject.create();
		HiddenRecordingActivity_.intent(context).start();
		return recordingPermissionPublishSubject.asObservable();
	}

	void onStreamPermissionGranted(Intent intent) {
		mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, intent);
		recordingPermissionPublishSubject.onNext(true);
	}

	void onStreamPermissionDenied() {
		recordingPermissionPublishSubject.onNext(false);
	}

	private Observable<Boolean> hasSystemPermission() {
		return RxPermissions.getInstance(context).request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}
}
