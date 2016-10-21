package com.frlgrd.streamzone.ui;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.frlgrd.streamzone.R;
import com.frlgrd.streamzone.core.recording.RecordingManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.EActivity;

import rx.functions.Action1;

@EActivity(value = R.layout.activity_main)
public class MainActivityActivity extends AppCompatActivity {

	@Bean RecordingManager recordingManager;

	@CheckedChange
	void toggle(boolean isChecked) {
		Log.d("", "");
		if (isChecked) {
			recordingManager.requestPermissions()
					.subscribe(new Action1<Boolean>() {
						@Override public void call(Boolean granted) {
							Log.d("", "");
						}
					});
		} else {

		}
	}
}
