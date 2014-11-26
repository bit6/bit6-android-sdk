package com.bit6.samples.demo;

import com.bit6.sdk.Bit6;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IncomingCallReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, IncomingCallActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// Send Call information to the IncomingCallActivity
		i.putExtra(Bit6.INTENT_EXTRA_DIALOG, intent.getParcelableExtra(Bit6.INTENT_EXTRA_DIALOG));
		context.startActivity(i);
	}
}
