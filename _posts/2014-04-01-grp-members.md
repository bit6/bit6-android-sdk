---
category: groups
title: 'Members'
---

### Join a Group

```java
// role is one of constants from Group.Permissions class, e.g Group.Permissions.ROLE_USER
bit6.joinGroup(groupId, role, handler);
```

### Leave a Group

```java
bit6.leaveGroup(groupId, handler);
```

### Invite a Member to a Group
```java
bit6.inviteGroupMember(groupId, identity, role, handler);
```

### Kick a Member from a Group
```java
bit6.kickGroupMember(groupId, identity, handler);
```
