---
category: authentication
title: 'Username'
---

A username is case-insensitive and must consist of alphanumeric characters, e.g. `usr:john` or  `usr:test123`.

### Create user account

Create a new user account with a username identity and a password.

```java
Address identity = Address.fromParts(Address.KIND_USERNAME, "john");

bit6.signup(identity, pass, new ResultCallback() {
    @Override
    public void onResult(boolean success, String msg) {
        if (success) {
            Log.v(TAG, "success " + msg);
        } else {
            Log.v(TAG, "error " + msg);
        }
    }
});
```

### Login

Login into an existing account using an Identity and a password.

```java
Address identity = Address.fromParts(Address.KIND_USERNAME, "john");

bit6.login(identity, pass, new ResultCallback() {
    @Override
    public void onResult(boolean success, String msg) {
        if (success) {
            Log.v(TAG, "success " + msg);
        } else {
            Log.v(TAG, "error " + msg);
        }
    }
});
```
