---
title: 'Authentication'
layout: nil
---

### Create user account

Create a new user account with a username identity.

```java
Bit6 bit6 = Bit6.getInstance();

Address identity = Address.fromParts(Address.KIND_USERNAME, username);

bit6.signup(identity, pass, new ResultCallback() {
	@Override
	public void onResult(boolean success, String msg) {
		if (success) {
			Log.e("onResponse", "success "+msg);
		} else {
			Log.e("onResponse", "fail "+msg);
		}
	}
});
```

### Login

Login into an existing account using an Identity and a password.

```java
Address identity = Address.fromParts(Address.KIND_USERNAME, username);

bit6.login(identity, pass, new ResultCallback() {
	@Override
	public void onResult(boolean success, String msg) {
		if (success) {
			Log.e("onResponse", "success "+msg);
		} else {
			Log.e("onResponse", "fail "+msg);
			}
		}
});
```


### Logout

```java
bit6.logout();
```


### Check if the user is logged in

```java
if (bit6.isUserLoggedIn()) {
	Log.e(TAG, "Logged in")
} else {
	Log.e(TAG, "Not logged in")
}
```