---
title: 'Getting Started'
---

### 1. Get Bit6 API Key
Go to [Dashboard](https://dashboard.bit6.com/) and get the API Key for your app.


### 2. Add Bit6 SDK to your Android Studio project

Add Bit6 SDK Maven repository in your project `build.gradle` file

```java
allprojects {
    repositories {
        jcenter()
            maven { url "https://raw.githubusercontent.com/bit6/bit6-android-sdk/master/releases/" }
    }
}
```

Add Bit6 SDK dependency in your module `build.gradle`. Bit6 UI Library is an optional module with common UI components for handling incoming and ongoing calls, message lists etc.

```java
dependencies {
    // Required Bit6 SDK module
    compile "com.bit6.sdk:bit6-sdk:{{site.version}}"
    // Optional Bit6 UI components
    // compile "com.bit6.sdk:bit6-ui:{{site.version}}"
}
```

### 3. Setup Application class

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

### 4. Manifest.xml

Assuming `your.package.name` is the package name for your application,
you need to do the following:

Specify the required permissions:

```xml
<!-- Internet access - Allows applications to connect to the network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<!-- Incoming call handling - Allows to keep the phone from sleeping or screen from dimming -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<!-- Incoming call handling - Access to the vibration effects -->
<uses-permission android:name="android.permission.VIBRATE" />
<!-- Voice/Video calling - Allows an application to record audio -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<!-- Voice/Video calling - Allows an application to modify global audio settings -->
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<!-- Voice/Video calling, photo/video messages - To access camera services -->
<uses-permission android:name="android.permission.CAMERA" />
<!-- Photo/Video messages - Allows an application to write to external storage. -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<!-- Location messages - To access location services -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- GCM push notifications -->
<permission
    android:name="your.package.name.permission.C2D_MESSAGE"
    android:protectionLevel="signature" />

<uses-permission android:name="your.package.name.permission.C2D_MESSAGE" />
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />

```

Modify `<application>` element:

```xml
<application>
  <!-- Required for applications which use Google Play Services. -->
  <meta-data
      android:name="com.google.android.gms.version"
      android:value="@integer/google_play_services_version" />

  <!-- Bit6 Content Provider -->
  <provider
      android:name="com.bit6.sdk.db.Bit6ContentProvider"
      android:authorities="your.package.name"
      android:exported="false" />

  <!-- Bit6 receives GCM push notifications -->
  <receiver
      android:name="com.bit6.sdk.push.GcmBroadcastReceiver"
      android:permission="com.google.android.c2dm.permission.SEND" >
      <intent-filter>
          <action android:name="com.google.android.c2dm.intent.RECEIVE" />
          <category android:name="your.package.name" />
      </intent-filter>
  </receiver>

  <!-- Bit6 handles GCM push notifications -->
  <service android:name="com.bit6.sdk.push.PushIntentService" />

  <!-- Your custom receiver for incoming calls -->
  <receiver android:name=".IncomingCallReceiver" android:enabled="true">
      <intent-filter>
          <action android:name="your.package.name.BIT6_INCOMING_CALL" />
      </intent-filter>
  </receiver>

  <!-- Your custom receiver for incoming messages -->
  <receiver android:name=".IncomingMessageReceiver" android:enabled="true">
      <intent-filter>
          <action android:name="your.package.name.BIT6_INCOMING_MESSAGE" />
      </intent-filter>
  </receiver>

</application>
```

### 5. Configure GCM push notifications

Get GCM Project Number and Server Key:

  - Go to Google [Dev Console](https://console.developers.google.com)
  - Select your project, note the Project Number
  - Click on API Manager or Use Google APIs)
  - In Overview, click on 'Cloud Messaging for Android', make sure it is enabled
  - Click on 'Credentials' in the right column
  - Click 'Add credentials' -> 'API key', then select 'Server Key'
  - Specify any name, click 'Create'
  - You will see the Server Key to use for GCM

Enter the Project Number and Server Key in Bit6 Dashboard:

  - Select your app in the Dashboard
  - Click Settings in the left menu, then go to Push Notifications tab.
  - Enter Project Number and Server Key in the GCM section



### 6. Enable ADM push notifications (optional)

To support push notifications on Amazon devices you need to modify `Manifest.xml`

Add `xmlns:amazon="http://schemas.amazon.com/apk/res/android"` into `<manifest>` element.

Specify the required permissions:

```xml
<!-- ADM Push Messaging -->
<permission
    android:name="your.package.name.permission.RECEIVE_ADM_MESSAGE"
    android:protectionLevel="signature" />

<uses-permission android:name="your.package.name.permission.RECEIVE_ADM_MESSAGE" />
<uses-permission android:name="com.amazon.device.messaging.permission.RECEIVE" />
```

Modify `<application>` element:

```xml
<!-- ADM push support -->
<service
    android:name="com.bit6.sdk.push.AdmMessageHandler"
    android:exported="false" />
<amazon:enable-feature android:name="com.amazon.device.messaging"
    android:required="false" />

<!-- Bit6 receives ADM push notifications -->
<receiver android:name="com.bit6.sdk.push.AdmMessageHandler$Receiver"
    android:permission="com.amazon.device.messaging.permission.SEND">
    <intent-filter>
        <action android:name="com.amazon.device.messaging.intent.REGISTRATION" />
        <action android:name="com.amazon.device.messaging.intent.RECEIVE" />
        <category android:name="your.package.name" />
    </intent-filter>
</receiver>
```
