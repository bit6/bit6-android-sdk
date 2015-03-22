---
category: messaging
title: 'Notifications'
---


### Real-Time Notifications

Get notified when another user is typing a message, or on any other real-time notification.

```java
bit6.addRtNotificationListener(new RtNotificationListener() {
    public void onTypingReceived(String from) {
        Log.v(TAG, "Typing from: " + from);
    }
    public void onNotificationReceived(String from, String type, JSONObject data) {
        Log.e(TAG, "Notification: " + data.toString());      
    }
});
```

### Send Typing Notification

```java
String destination = "usr:john"
bit6.sendTypingNotification(Address.parse(destination));
```

### Send Custom Notification

```java
String destination = "usr:john"
bit6.sendNotification(Address.parse(destination), "mytype");
```
