
package com.bit6.samples.demo;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.CallClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = null;
        CallClient callClient = Bit6.getInstance().getCallClient(); 
        if (callClient != null && callClient.getRtcDialogs().size() > 0) {
            i = new Intent(context, CallActivity.class);
        } else {
            i = new Intent(context, IncomingCallActivity.class);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Send Call information to the IncomingCallActivity
        i.putExtras(intent);
        context.startActivity(i);
    }
}
