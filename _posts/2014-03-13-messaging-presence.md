---
category: messaging
title: 'Notifications'
---


### Real-Time Notifications

Get notifications when a message status changes (for example, to delivered), a new notification has been received, or another user is typing a message to you.

**Step 1.** `Implement RtNotificationListener`

**Step 2.** `bit6.addRtNotificationListener(this);`


```java
bit6.addRtNotificationListener(new RtNotificationListener() {

    public void onTypingReceived(String from) {
        Log.e("onTypingReceived()", from);
    }
    
    public void onNotificationReceived(String from, String type, JSONObject data) {
        Log.e("onNotificationReceived()", "" + data.toString());      
    }
})
```

### Send Typing Notification

```java
String destination = "usr:john"
bit6.sendTypingNotification(Address.parse(destination));
```

### Send Custom Notification
```java
String destination = "usr:john"
bit6.sendNotification(Address.parse(destination), "type");
```
