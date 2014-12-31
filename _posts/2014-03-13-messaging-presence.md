---
category: basic messaging
title: 'Presence'
---


### Real-Time Notifications

Get notifications when a message status changes (for example, to delivered), a new message has been received, or another user is typing a message to you.

**Step 1.** `Implement RtNotificationListener`

**Step 2.** `bit6.addRtNotificationListener(this);`


```java
bit6.addRtNotificationListener(new RtNotificationListener() {

    public void onTyping(JSONObject json) {
        Log.e("onTyping()", "" + json.toString());
    }

    public void onMessageUpdate(JSONObject json) {
        Log.e("onMessageUpdate()", "" + json.toString());
    }

    public void onNewMessage(JSONObject json) {
        Log.e("onNewMessage()", "" + json.toString());      
    }
})
```

### Send Typing Notification

```java
String destination = "usr:john"
bit6.sendTypingNotification(Address.parse(destination));
```
