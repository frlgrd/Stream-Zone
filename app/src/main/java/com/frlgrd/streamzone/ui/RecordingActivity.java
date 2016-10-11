package com.frlgrd.streamzone.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ToggleButton;

import com.frlgrd.streamzone.App;
import com.frlgrd.streamzone.R;
import com.frlgrd.streamzone.core.event.MediaProjectionInfosHolder;
import com.frlgrd.streamzone.core.event.RecordServiceReady;
import com.frlgrd.streamzone.core.event.StartRecording;
import com.frlgrd.streamzone.core.event.StopRecording;
import com.frlgrd.streamzone.core.recording.RecordingService;
import com.squareup.otto.Subscribe;
import com.tbruyelle.rxpermissions.RxPermissions;

public class RecordingActivity extends AppCompatActivity {

	private static final int REQUEST_CAPTURE = 42;

	private MediaProjectionManager projectionManager;
	private MediaProjectionInfosHolder projectionInfoHolder;

	private ToggleButton toggleButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toggleButton = (ToggleButton) findViewById(R.id.toggle);
		toggleButton.setOnClickListener(RecordingActivity.this::onToggleScreenShare);
		RxPermissions.getInstance(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
				.subscribe(granted -> {
					if (granted) {
						projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
						startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE);
					}
				});

		App.BUS.register(this);
	}

	@Override
	protected void onDestroy() {
		App.BUS.unregister(this);
		super.onDestroy();
	}

	@Subscribe
	public void onServiceIsReady(RecordServiceReady ready) {
		App.BUS.post(projectionInfoHolder);
		toggleButton.setEnabled(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == REQUEST_CAPTURE) {
			if (!RecordingService.ready) {
				startService(new Intent(this, RecordingService.class));
				projectionInfoHolder = new MediaProjectionInfosHolder(data, resultCode);
			} else {
				toggleButton.setEnabled(true);
			}
		}
	}

	public void onToggleScreenShare(View view) {
		if (((ToggleButton) view).isChecked()) {
			App.BUS.post(new StartRecording());
		} else {
			App.BUS.post(new StopRecording());
		}
	}
}
