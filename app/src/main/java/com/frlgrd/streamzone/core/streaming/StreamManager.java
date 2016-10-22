package com.frlgrd.streamzone.core.streaming;

import android.net.wifi.WifiManager;
import android.os.ParcelFileDescriptor;
import android.text.format.Formatter;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

import java.io.IOException;
import java.net.Socket;

@EBean
public class StreamManager {

	private static final int RTSP_PORT = 554;

	@SystemService WifiManager wifiManager;

	@SuppressWarnings("deprecation") /* no need ipv6*/ private String getIP() {
		return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
	}

	public ParcelFileDescriptor getParcelFileDescriptor() throws IOException {
		return ParcelFileDescriptor.fromSocket(new Socket(getIP(), RTSP_PORT));
	}
}
