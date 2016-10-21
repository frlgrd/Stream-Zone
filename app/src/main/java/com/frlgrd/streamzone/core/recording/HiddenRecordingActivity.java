package com.frlgrd.streamzone.core.recording;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;

@EActivity
public class HiddenRecordingActivity extends Activity {

	private static final int REQUEST_CODE = 42;

	@Bean RecordingManager recordingManager;
	@SystemService MediaProjectionManager mediaProjectionManager;

	@AfterInject void afterInject() {
		startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			recordingManager.onStreamPermissionGranted(data);
		} else {
			recordingManager.onStreamPermissionDenied();
		}
		finish();
	}
}
