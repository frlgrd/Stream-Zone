package com.frlgrd.streamzone.ui;

import android.Manifest;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ToggleButton;

import com.frlgrd.streamzone.R;
import com.frlgrd.streamzone.core.event.Otto;
import com.frlgrd.streamzone.core.event.RecordServiceReadyEvent;
import com.frlgrd.streamzone.core.event.StartRecordingEvent;
import com.frlgrd.streamzone.core.event.StopRecordingEvent;
import com.frlgrd.streamzone.core.recording.RecordingHelper;
import com.frlgrd.streamzone.core.recording.RecordingService_;
import com.squareup.otto.Subscribe;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import rx.functions.Action1;

@SuppressWarnings("unused")
@EActivity(value = R.layout.activity_main)
public class RecordingActivity extends AppCompatActivity {

	private static final int REQUEST_CAPTURE = 42;
	@ViewById ToggleButton toggle;
	@SystemService MediaProjectionManager projectionManager;
	@Bean Otto otto;
	@Bean RecordingHelper recordingHelper;

	private StartRecordingEvent startRecordingEvent;

	@AfterViews void afterViews() {
		otto.register(this);

		toggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onToggleScreenShare();
			}
		});

		if (!recordingHelper.isRecording()) {
			checkPermissions();
		}

		toggle.setEnabled(recordingHelper.isReady());
		toggle.setChecked(recordingHelper.isRecording());
	}

	private void checkPermissions() {
		RxPermissions.getInstance(this).request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				.subscribe(new Action1<Boolean>() {
					@Override
					public void call(Boolean granted) {
						if (granted) {
							startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE);
						}
					}
				});
	}

	@Override protected void onDestroy() {
		otto.unregister(this);
		super.onDestroy();
	}

	@Subscribe public void onServiceIsReady(RecordServiceReadyEvent ready) {
		toggle.setEnabled(true);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == REQUEST_CAPTURE) {
			if (!recordingHelper.isReady()) {
				RecordingService_.intent(getApplication()).start();
				startRecordingEvent = new StartRecordingEvent(data, resultCode);
			} else {
				toggle.setEnabled(true);
			}
		}
	}

	private void onToggleScreenShare() {
		if (toggle.isChecked()) {
			otto.post(startRecordingEvent);
		} else {
			otto.post(new StopRecordingEvent());
		}
	}
}
