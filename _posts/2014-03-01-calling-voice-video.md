---
category: calling
title: 'Voice & Video Calls'
layout: nil
---

### Make Call

Register `InCallActivity` in Manifest.xml

```java
<activity android:name="com.bit6.sdk.ui.InCallActivity"
    android:label="@string/app_name"
    android:configChanges="orientation|screenSize"
    android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen">
</activity> 
```

To make a call to a destination:

```java
RtcDialog dialog = bit6.startCall(to, isVideo);
dialog.launchInCallActivity(this);
```  

### Receive Call
**Step 1.** Register Broadcast Receiver for `com.bit6.intent.INCOMING_CALL` intent in your Manifest.xml

```java
<receiver android:name=".IncomingCallReceiver" android:enabled="true">
    <intent-filter>
        <action android:name="com.bit6.intent.INCOMING_CALL"></action>
    </intent-filter>
</receiver>
```

**Step 2.** Create a receiver class which extends BroadcastReceiver.

```java
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
```

**Step 3.** Get `RtcDalog` from intent in your activity

Implement `RtcDialog.StateListener` to receive call state changes.

```
@Override
public void onStateChanged(RtcDialog d, int state) {
    if (state == RtcDialog.END) {
        finish();
    }
}

RtcDialog dialog = bit6.getDialogFromIntent(getIntent());
dialog.addStateListener(this);

```

To get caller name call `dialog.getOther();`

To check if it is video or voice call then call `dialog.hasVideo()`

Call `dialog.launchInCallActivity(this);` to answer the call

Call `dialog.hangup();` to reject the call
