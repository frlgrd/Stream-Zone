package com.frlgrd.streamzone.core.event;

import android.content.Intent;

public class MediaProjectionInfosHolder {
	private Intent intent;
	private int resultCode;

	public MediaProjectionInfosHolder(Intent intent, int resultCode) {
		this.intent = intent;
		this.resultCode = resultCode;
	}

	public int getResultCode() {
		return resultCode;
	}

	public Intent getIntent() {
		return intent;
	}
}
