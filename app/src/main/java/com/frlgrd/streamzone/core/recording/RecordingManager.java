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

@EBean(scope = EBean.Scope.Singleton)
public class RecordingManager {

	@SystemService MediaProjectionManager mediaProjectionManager;
	@RootContext Context context;

	private MediaProjection mediaProjection;

	public void checkPermission(Context context) {
		HiddenRecordingActivity_.intent(context).start();
	}

	void onStreamPermissionGranted(Intent intent) {
		mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, intent);
	}

	void onStreamPermissionDenied() {

	}

	public Observable<Boolean> canRecord(Context context) {
		return RxPermissions.getInstance(context).request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

}
