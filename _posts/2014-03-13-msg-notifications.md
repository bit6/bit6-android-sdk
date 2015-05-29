---
category: messaging
title: 'Notifications'
---

### Real-Time Notifications

Bit6 notification capabilities are accessed via `NotificationClient`

```java
NotificationClient notificationClient = bit6.getNotificationClient();
```

### Send Typing Notification

```java
Address to = Address.parse("usr:john");
notificationClient.sendTypingNotification(to);
```

### Send Custom Notification

```java
Address to = Address.parse("usr:john");
notificationClient.sendNotification(to, "mytype");
```

### Receiving Notifications

Get notified when connection is established or closed, another user is typing a message, or on any other real-time notification.

```java
notificationClient.addListener(new NotificationClient.Listener() {
    public void onTypingReceived(String from) {
        Log.v(TAG, "Typing from: " + from);
    }
    public void onNotificationReceived(String from, String type, JSONObject data) {
        Log.e(TAG, "Notification: " + data.toString());
    }
    public void onConnectedChanged(boolean isConnected) {
        Log.e(TAG, "RT is connected "+isConnected);
    }
});
```
