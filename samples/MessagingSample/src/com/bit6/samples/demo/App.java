
package com.bit6.samples.demo;

import android.app.Application;
import android.content.SharedPreferences;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.LifecycleHelper;

public class App extends Application {

    final static String
            PROD_API_KEY = "your_api_key",
            DEV_API_KEY = "your_api_key";

    final static String
            PREF_NAME = "env",
            PREF_ENV_ID = "envId";

    public void onCreate() {
        super.onCreate();

        Bit6 bit6 = Bit6.getInstance();

        // Select the environment to connect to
        // Usually you can just use constants but in this demo we allow
        // switching environments (see MainActivity)
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int env = pref.getInt(PREF_ENV_ID, Bit6.PRODUCTION);
        String apikey = env == Bit6.PRODUCTION ? PROD_API_KEY : DEV_API_KEY;

        // Initialize Bit6
        bit6.init(getApplicationContext(), apikey, env);

        // Manage Bit6 lifecycle automatically
        registerActivityLifecycleCallbacks(new LifecycleHelper(bit6));
    }

}
