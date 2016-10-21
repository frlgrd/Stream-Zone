package com.frlgrd.streamzone.ui;

import android.support.v7.app.AppCompatActivity;
import android.widget.ToggleButton;

import com.frlgrd.streamzone.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(value = R.layout.activity_main)
public class MainActivityActivity extends AppCompatActivity {

	@ViewById ToggleButton toggle;

	@AfterViews
	void afterViews() {

	}
}
