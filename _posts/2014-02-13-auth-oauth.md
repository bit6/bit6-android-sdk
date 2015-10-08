---
category: authentication
title: 'OAuth'
---

Bit6 integrates with various OAuth1 and OAuth2 providers for simplified user authentication. Check the [sample apps](https://github.com/bit6/bit6-android-samples) for details. As an example, we will describe Facebook authentication process.

### Sign in with Facebook

Step 1. Create a [Facebook App](https://developers.facebook.com/apps). Copy its App ID and Secret into [Bit6 Dashboard](https://dashboard.bit6.com/).

Step 2. Follow all the instructions described in [Facebook Login](https://developers.facebook.com/docs/facebook-login/android) documentation.

Step 3. Change the callback function for the `loginButton` to the following:

```java
// Callback registration
loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
    @Override
    public void onSuccess(LoginResult loginResult) {
        // OAuth2 access token
        AccessToken token = loginResult.getAccessToken();
        // Send the token to Bit6
        bit6.getSessionClient().oauth("facebook", token.getToken(), new ResultHandler() {
            @Override
            public void onResult(boolean success, String msg) {
                if (success) {
                    Log.v(TAG, "success " + msg);
                } else {
                    Log.v(TAG, "error " + msg);
                }
            }
        });
    }

    @Override
    public void onCancel() {
        // App code
    }

    @Override
    public void onError(FacebookException exception) {
        // App code
    }
});
```
