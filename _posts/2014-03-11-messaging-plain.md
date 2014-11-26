---
category: basic messaging
title: 'Text Messages'

layout: nil
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

