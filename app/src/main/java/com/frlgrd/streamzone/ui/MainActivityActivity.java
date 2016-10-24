package com.frlgrd.streamzone.ui;

import android.support.v7.app.AppCompatActivity;
import android.widget.ToggleButton;

import com.frlgrd.streamzone.R;
import com.frlgrd.streamzone.core.recording.RecordingManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(value = R.layout.activity_main)
public class MainActivityActivity extends AppCompatActivity {

	@Bean RecordingManager recordingManager;
	@ViewById ToggleButton toggle;

	@AfterViews void afterViews() {
		toggle.setChecked(recordingManager.isRecording());
		toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				recordingManager.requestPermissions()
						.subscribe(granted -> {
							if (granted) {
								recordingManager.start();
							}
						});
			} else {
				recordingManager.stop();
			}
		});
	}
}
