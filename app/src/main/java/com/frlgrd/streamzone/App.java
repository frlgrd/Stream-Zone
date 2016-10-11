package com.frlgrd.streamzone;

import android.app.Application;

import com.squareup.otto.Bus;

public class App extends Application {
	public static final Bus BUS = new Bus();
}
