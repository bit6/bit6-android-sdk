package com.bit6.samples.demo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class IncomingMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getExtras() != null){
			String content = intent.getExtras().getString("content");
			String senderName = intent.getExtras().getString("senderName");
			sendNotification(context, content, senderName);
		}
		

	}

	private void sendNotification(Context context, String msg, String senderName) {

		NotificationManager notificationmanager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra("dest", senderName);

		PendingIntent contentIntent = PendingIntent.getActivity(context,
				msg.hashCode(), intent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(senderName)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		builder.setContentIntent(contentIntent);
		builder.setAutoCancel(true);
		notificationmanager.notify(1, builder.build());
	}

}
