---
category: messaging
title: 'Text Messages'
---


### Send Text Message

```java
String content = "This is a text message";
Address to = Address.parse(destination);
Message m =  Message.newMessage(to).text(content);
bit6.sendMessage(m, statusListener);
```

To follow the message status as it is being sent implement `MessageStatusListener`.

```java
import com.bit6.sdk.MessageStatusListener;

@Override
public void onMessageStatusChanged(Message m, int state) {
	if (state == Message.STATUS_SENDING) {

	} else if (state == Message.STATUS_FAILED) {

	}
}
```


### Delete a Message

```java
bit6.deleteMessage(messageId, new ResultCallback() {
	@Override
	public void onResult(boolean success, String msg) {

	}
});
```

### Get Messages

```java
Cursor cursor = bit6.getConversations();
```


### Get Messages in a Conversation

```java
Cursor cursor = bit6.getConversation(destination);
```

### Handle broadcast intent when new message arrives
**Step 1.** Register Broadcast Receiver for `your.package.name.intent.INCOMING_MESSAGE` intent in your Manifest.xml

```java
<receiver android:name=".IncomingMessageReceiver" android:enabled="true">
    <intent-filter>
        <action android:name="your.package.name.intent.INCOMING_MESSAGE"></action>
    </intent-filter>
</receiver>
```

**Step 2.** Create a receiver class which extends BroadcastReceiver.

```java
public class IncomingMessageReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
	    if(intent.getExtras() != null){
			String content = intent.getExtras().getString("content");
			String senderName = intent.getExtras().getString("senderName");
		}
	}
}
```

