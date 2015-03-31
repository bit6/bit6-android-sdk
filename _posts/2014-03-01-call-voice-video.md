---
category: calling
title: 'Voice & Video Calls'
---

### Default In-Call UI

Register default `InCallActivity` in Manifest.xml. You can build custom In-Call UI as described in a [separate section](#calling-ui).

```java
<activity android:name="com.bit6.sdk.ui.InCallActivity"
    android:label="@string/app_name"
    android:configChanges="orientation|screenSize"
    android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen">
</activity> 
```

### Start an Outgoing Call

```java
// Initiate a call
Address to = Address.parse("usr:john");
RtcDialog dialog = bit6.getCallClient().startCall(to, isVideo);
// Launch the default InCall activity
dialog.launchInCallActivity(this);
```  


### Handle an Incoming Call

**Step 1.** Register Broadcast Receiver for `your.package.name.BIT6_INCOMING_CALL` intent in your Manifest.xml

```java
<receiver android:name=".IncomingCallReceiver" android:enabled="true">
    <intent-filter>
        <action android:name="your.package.name.BIT6_INCOMING_CALL"></action>
    </intent-filter>
</receiver>
```

**Step 2.** Create a receiver class which extends BroadcastReceiver.

```java
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
```


**Step 3.** Implement `IncomingCallActivity`

Get call controller and display UI for answering / rejecting a call

```java
// Get RtcDialog (call controller) for this call
RtcDialog dialog = bit6.getCallClient().getDialogFromIntent(getIntent());
dialog.addStateListener(this);

// Get caller information
String caller = dialog.getOther();
// Is this a Video call
boolean video = dialog.hasVideo();

// Reject / hangup this call
dialog.hangup();
```

Implement `RtcDialog.StateListener` to receive call state changes - if the caller hangs up the call before the user had a chance to answer or reject it.

```java
@Override
public void onStateChanged(RtcDialog d, int state) {
    if (state == RtcDialog.END) {
        finish();
    }
}

```

**Step 4.** Accept the call and show In-Call UI

```java
// Answer the call and launch the default In-Call Activity
dialog.launchInCallActivity(this);
```

