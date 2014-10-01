package com.bit6.samples.demo;

import android.app.Application;

import com.bit6.sdk.Bit6;

public class App extends Application {

	public final static String SENDER_ID = "YOUR_SENDER_ID";
	public final static String API_KEY = "YOUR_API_KEY";

	@Override
	public void onCreate() {
		super.onCreate();
		
		Bit6.getInstance().init(getApplicationContext(), API_KEY, this, SENDER_ID);

	}

}
