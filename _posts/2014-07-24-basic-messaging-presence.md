---
category: basic messaging
title: 'Presence'

layout: nil
---

### Notify the Recipient that the Sender is Typing

```java
String destination = "usr:john"
bit6.sendTypingNotification(Address.parse(destination));
```

### Detect when the Recipient is Typing

Receiving the presence notifications is implemented as a part of `MessageListener` - click [here](/#text-messaging).