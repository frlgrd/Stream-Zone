package com.frlgrd.streamzone.core.recording;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.frlgrd.streamzone.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;

@SuppressWarnings("WeakerAccess")
@EBean
public class RecordingNotificationHelper implements RecordingHelper.OnRecordSateChangedListener {

	@SystemService NotificationManager notificationManager;
	@SuppressWarnings("WeakerAccess") @Bean RecordingHelper recordingHelper;
	@RootContext Context context;

	@AfterInject
	void afterInject() {
		recordingHelper.registerRecordingState(this);
	}

	@Override public void onRecordSateChanged(boolean isRecording) {

		Intent intent = new Intent();
		intent.setAction(isRecording ? RecordingHelper.ACTION_RECORD_START : RecordingHelper.ACTION_RECORD_STOP);
		PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Action action = new NotificationCompat.Action(
				isRecording ? R.drawable.ic_pause : R.drawable.ic_play,
				context.getString(isRecording ? R.string.stop_stream : R.string.continue_stream),
				pendingIntentYes
		);

		Notification notification = new android.support.v7.app.NotificationCompat.Builder(context)
				.setContentTitle("New mail from " + "test@gmail.com")
				.setContentText("Subject")
				.setSmallIcon(R.drawable.ic_streaming_on)
				.setContentIntent(pendingIntentYes)
				.setOngoing(!isRecording)
				.addAction(action)
				.build();

		notificationManager.notify(10, notification);
	}
}
