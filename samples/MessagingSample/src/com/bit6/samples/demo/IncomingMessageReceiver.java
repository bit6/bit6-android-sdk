
package com.bit6.samples.demo;

import com.bit6.sdk.Bit6;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class IncomingMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String content = intent.getStringExtra(Bit6.INTENT_EXTRA_CONTENT);
        String senderName = intent.getStringExtra(Bit6.INTENT_EXTRA_NAME);
        if (content != null && senderName != null) {
            showNotification(context, content, senderName);
        }
    }

    // Helper method for showing a notification that leads to a chat screen
    // Also called from IncomingCallActivity
    static void showNotification(Context context, String text, String senderName) {

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_EXTRA_DEST, senderName);

        PendingIntent contentIntent = PendingIntent.getActivity(context, text.hashCode(), intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(senderName)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, builder.build());
    }

}
