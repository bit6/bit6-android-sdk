---
category: authentication
title: 'Session'
---

Each user in the system has one or more identities - user id, username, email, facebook id, google account, phone number etc. Identities are required for user authentication, managing contacts, identifying user's network. An identity is represented by a URI.

Bit6 supports various authentication mechanisms described in the following sections.

The session capabilities are accessed via `SessionClient`.

```java
SessionClient sessionClient = bit6.getSessionClient();
```

### Authenticate as anonymous user

```java
sessionClient.anonymous(resultHandler);
```

### Check if the user is authenticated

```java
if (sessionClient.isAuthenticated()) {
	Log.v(TAG, "Authenticated")
} else {
	Log.v(TAG, "Not Authenticated ")
}
```


### Logout

```java
sessionClient.logout();
```
