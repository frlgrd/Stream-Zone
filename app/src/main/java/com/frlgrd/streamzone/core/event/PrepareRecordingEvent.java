package com.frlgrd.streamzone.core.event;

import android.content.Intent;

public class PrepareRecordingEvent {
	private Intent intent;
	private int resultCode;

	public PrepareRecordingEvent(Intent intent, int resultCode) {
		this.intent = intent;
		this.resultCode = resultCode;
	}

	public Intent getIntent() {
		return intent;
	}

	public int getResultCode() {
		return resultCode;
	}
}
