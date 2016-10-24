package com.frlgrd.streamzone.core.streaming;

import android.net.wifi.WifiManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.frlgrd.streamzone.core.recording.RecordingManager;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.channels.Channels;

@EBean
public class StreamManager {

	private static final int BUFFER_SIZE = 1024;
	@SystemService WifiManager wifiManager;
	@Bean RecordingManager recordingManager;

	@Background
	public void stream() {
		InputStream inputStream = null;
		try {
			RandomAccessFile file = new RandomAccessFile(RecordingManager.OUTPUT_FILE_PATH, "rw");
			inputStream = Channels.newInputStream(file.getChannel());
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			long pointerPosition = 0;
			file.seek(pointerPosition);
			while ((line = r.readLine()) != null && recordingManager.isRecording()) {

			}
			Log.e("STREAM", String.valueOf(pointerPosition));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public FileDescriptor getFileDescriptor() {
		Socket socket = new Socket();
		return ParcelFileDescriptor.fromSocket(socket).getFileDescriptor();
	}
}
