package com.frlgrd.streamzone.ui;

import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.frlgrd.streamzone.R;
import com.frlgrd.streamzone.core.recording.RecordingManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import rx.functions.Action1;

@EActivity(value = R.layout.activity_main)
public class MainActivityActivity extends AppCompatActivity {

	@Bean RecordingManager recordingManager;
	@ViewById ToggleButton toggle;

	@AfterViews
	void afterViews() {
		toggle.setChecked(recordingManager.isRecording());
		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					recordingManager.requestPermissions()
							.subscribe(new Action1<Boolean>() {
								@Override public void call(Boolean granted) {
									if (granted) {
										recordingManager.start();
									}
								}
							});
				} else {
					recordingManager.stop();
				}
			}
		});
	}
}
