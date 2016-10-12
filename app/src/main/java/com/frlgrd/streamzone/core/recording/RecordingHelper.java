package com.frlgrd.streamzone.core.recording;

import com.frlgrd.streamzone.core.event.Otto;
import com.frlgrd.streamzone.core.event.RecordServiceReadyEvent;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class RecordingHelper {

	@SuppressWarnings("WeakerAccess") @Bean Otto otto;

	private boolean isReady = false;
	private boolean isRecording = false;

	public boolean isReady() {
		return isReady;
	}

	void ready() {
		isReady = true;
		otto.post(new RecordServiceReadyEvent());
	}

	public boolean isRecording() {
		return isRecording;
	}

	void startRecording() {
		isRecording = true;
	}

	void stopRecording() {
		isRecording = false;
	}

	void recorderServiceKilled() {
		isRecording = false;
	}
}
