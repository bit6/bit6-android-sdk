---
title: 'Getting Started'
layout: nil
---

### Add Bit6 SDK to your Eclipse project

1. [Download](https://github.com/bit6/bit6-android-sdk/) the Bit6 SDK

2. Import `bit6-sdk` project into Eclipse

3. Add `bit6-sdk` as a library into your project:
  - right click on your project 
  - select Properties -> Android
  - click 'Add...'
  - choose bit6-sdk and click OK

<img style="max-width:100%; " src="images/project_properties.png"/>


### Get a Bit6 API Key

You will need an API key in order to initialize and use Bit6 SDK. Send an email to dev@bit6.com to receive an application key.

### Setup Application class

In your Application class

**Step 1.** Import Bit6: `import com.bit6.sdk.Bit6;`

**Step 2.** Add the following to `onCreate()` method:

```java
Bit6.getInstance().init(getApplicationContext(), apikey, this, gcmSenderId);
```

### Add to your Manifest.xml

```xml
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
<uses-permission android:name="android.permission.USE_CREDENTIALS" />

<permission
    android:name="com.bit6.permission.C2D_MESSAGE"
    android:protectionLevel="signature" />

<uses-permission android:name="com.bit6.permission.C2D_MESSAGE" />

<!-- This app has permission to register and receive data message. -->
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />

```
Add into the `<application>` element:

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
  <service android:name="com.bit6.sdk.gcm.GcmIntentService" />
</application>
```

