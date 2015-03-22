
package com.bit6.samples.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, IncomingCallActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Send Call information to the IncomingCallActivity
        i.putExtras(intent);
        context.startActivity(i);
    }
}
