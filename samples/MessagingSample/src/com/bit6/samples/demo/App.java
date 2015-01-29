package com.bit6.samples.demo;

import android.app.Application;
import android.content.SharedPreferences;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.LifecycleHelper;

public class App extends Application {	
	
	public final static String PROD_API_KEY = "your_api_key";
	public final static String DEV_API_KEY = "your_api_key";

	public void onCreate() {		
		super.onCreate();
		
		SharedPreferences sPref= getSharedPreferences("env", MODE_PRIVATE);
		String env = sPref.getString("env", null);
		Bit6 bit6 = Bit6.getInstance(); 
		bit6.init(getApplicationContext(), "dev".equals(env) ? DEV_API_KEY :  PROD_API_KEY);
		registerActivityLifecycleCallbacks(new LifecycleHelper(bit6));

	}

}
