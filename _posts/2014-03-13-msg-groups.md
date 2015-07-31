---
category: messaging
title: 'Groups'
---

### Get Groups

Get all groups that this user is a member of

```java
Cursor cursor = getContentResolver().query(Contract.Groups.CONTENT_URI,null, null, null, null);
```

### Create a Group

Create a new group. The current user will become the group administrator.

```java
Group g = Group.newGroup();
// Optional - specify the group name
g.setName(myGroupId);
// Optional - specify the group permissions
g.setPermission(Group.VIEW, Group.ROLE_ALL);
// Create the group
bit6.createGroup(g, handler);
```

### Leave a Group

```java
bit6.leaveGroup(groupId, handler);
```
