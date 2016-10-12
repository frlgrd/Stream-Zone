package com.frlgrd.streamzone.core.recording;

import com.frlgrd.streamzone.core.event.Otto;
import com.frlgrd.streamzone.core.event.RecordServiceReadyEvent;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.HashSet;
import java.util.Set;

@EBean(scope = EBean.Scope.Singleton)
public class RecordingHelper {

	public static final String ACTION_RECORD_START = "ACTION_RECORD_START";
	public static final String ACTION_RECORD_STOP = "ACTION_RECORD_STOP";

	@SuppressWarnings("WeakerAccess") @Bean Otto otto;

	private boolean isReady = false;
	private boolean isRecording = false;

	private Set<OnRecordSateChangedListener> listeners = new HashSet<>();

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

	private synchronized void setRecording(boolean recording) {
		boolean dispatchEvents = recording != isRecording;
		isRecording = recording;
		if (dispatchEvents) {
			for (OnRecordSateChangedListener changedListener : listeners) {
				changedListener.onRecordSateChanged(isRecording);
			}
		}
	}

	void startRecording() {
		setRecording(true);
	}

	void stopRecording() {
		setRecording(false);
	}

	void recorderServiceKilled() {
		setRecording(false);
		isReady = false;
	}

	public void registerRecordingState(OnRecordSateChangedListener listener) {
		listeners.add(listener);
	}

	public void unregisterRecordingState(OnRecordSateChangedListener listener) {
		listeners.remove(listener);
	}

	public interface OnRecordSateChangedListener {
		void onRecordSateChanged(boolean isRecording);
	}
}
