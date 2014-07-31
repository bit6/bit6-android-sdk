---
category: basic messaging
title: 'Text Messages'

layout: nil
---

### Send Text Message

```java
String content = "This is a text message";
Address to = Address.parse(destination);
bit6.sendMessage(to, content, new OnResponseReceived() {

	public void onResponse(boolean success, String msg) {
		if (success) {
			Toast.makeText(context, msg,Toast.LENGTH_LONG).show();
		
		} else {
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
	}
});
```

### Delete Text Message

```java
bit6.deleteMessage(messageId, new OnResponseReceived() {
	public void onResponse(boolean success, String msg) {

	}
});
```


### Get Messages

```java
Cursor cursor = bit6.getConversations();
```

### Listen to Changes in Messages

To know when a message has been added, a message status has been updated, or presence notification has been received, register `MessageListener` to Bit6 instance.

Implement `MessageListener`:

```java
bit6.addMessageListener(this);
```

```java
public void onTyping(JSONObject json) {
	Log.e("onTyping()", "" + json.toString());
}

public void onMessageUpdate(JSONObject json) {
	Log.e("onMessageUpdate()", "" + json.toString());
}

public void onNewMessage(JSONObject json) {
	Log.e("onNewMessage()", "" + json.toString());		
}
```

### Get Messages in a Conversation

Although messages do not have to be arranged in conversations, it is frequently convenient to have the messages sorted by destination.

```java
Cursor cursor bit6.getConversation(destination);
```