---
title: 'Getting Started'
---

### Get Bit6 API Key
Go to [Dashboard](https://dashboard.bit6.com/) and get the API Key for your app.


### Add Bit6 SDK to your Eclipse project

__Step 1.__ [Download](https://github.com/bit6/bit6-android-sdk/) the Bit6 SDK

__Step 2.__ Import `bit6-sdk` project into Eclipse

__Step 3.__ Add `bit6-sdk` as a library into your project:

  * right click on your project
  * select Properties -> Android
  * click 'Add...'
  * select bit6-sdk and click OK

<img class="shot" src="images/project_properties.png"/>


### Setup Application class

```java
import com.bit6.sdk.Bit6;
import com.bit6.sdk.LifecycleHelper;

public class App extends Application {
  // Override onCreate method to init the SDK
  public void onCreate() {
    super.onCreate();
    // Initialize Bit6 SDK
    Bit6 bit6 = Bit6.getInstance();
    bit6.init(getApplicationContext(), "MY_API_KEY");
    // Bit6 will be notified about lifecycle events for app activities
    registerActivityLifecycleCallbacks(new LifecycleHelper(bit6));
  }
}

```

### Manifest.xml

Specify the required permissions.

```xml
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
<uses-permission android:name="android.permission.USE_CREDENTIALS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<permission
    android:name="com.bit6.permission.C2D_MESSAGE"
    android:protectionLevel="signature" />

<uses-permission android:name="com.bit6.permission.C2D_MESSAGE" />

<!-- This app has permission to register and receive data message. -->
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />

```
Modify `<application>` element. `your.package.name` is the package name for your application.

```xml
<application>
  <!-- Required for applications which use Google Play Services. -->
  <meta-data android:name="com.google.android.gms.version"
   android:value="@integer/google_play_services_version" />

  <provider
      android:name="com.bit6.sdk.Bit6ContentProvider"
      android:authorities="your.package.name"
      android:exported="false" />
          
  <receiver
      android:name="com.bit6.sdk.gcm.GcmBroadcastReceiver"
      android:permission="com.google.android.c2dm.permission.SEND" >
      <intent-filter>

          <!-- Receives the actual messages. -->
          <action android:name="com.google.android.c2dm.intent.RECEIVE" />
          <category android:name="com.bit6" />
      </intent-filter>
  </receiver>

  <receiver android:name=".IncomingCallReceiver" android:enabled="true">
      <intent-filter>
          <action android:name="your.package.name.intent.INCOMING_CALL"></action>
      </intent-filter>
  </receiver>

  <receiver android:name=".IncomingMessageReceiver" android:enabled="true">
      <intent-filter>
          <action android:name="your.package.name.intent.INCOMING_MESSAGE"></action>
      </intent-filter>
  </receiver>

  <service android:name="com.bit6.sdk.gcm.GcmIntentService" />

</application>
```

