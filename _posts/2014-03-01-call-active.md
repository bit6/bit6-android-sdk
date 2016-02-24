---
category: calling
title: 'Active Calls'
---

Bit6 calling capabilities are accessed via `CallClient`.

```java
CallClient callClient = bit6.getCallClient();
```

When there are ongoing active calls, your app will need to present some form of UI to control the calls (end, mute etc) and render video streams if applicable. The following steps happen:

1. Bit6 SDK determines that a new active call has been initiated, either incoming or outgoing. The SDK broadcasts `your.package.name.BIT6_CALL_ADDED` intent.

2. The application code needs to handle this intent to create or update the in-call UI.

3. The in-call UI is notified about changes to the active calls and can control them as well. For example, when all the active calls end, the app may decide to hide it.


Steps 2 and 3 can be implemented by either (a) using Bit6 UI Library components or (b) developing custom code. Sample code for both options is available in Bit6 samples repo and is also briefly described below.

Note that for this example we assume that the in-call UI is displayed as an Activity but it can be easily implemented as a Fragment or a View.

In both cases the app code will need to have a container (`CallActivity` in our example) that will display the UI elements and the video streams. It also handles `your.package.name.BIT6_CALL_ADDED` intent. In your `Manifest.xml`:

```xml
<activity
    android:name=".CallActivity"
    android:configChanges="orientation|screenSize"
    android:launchMode="singleTask"
    android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen">
        <intent-filter>
            <action android:name="your.package.name.BIT6_CALL_ADDED" />
            <category android:name="android.intent.category.DEFAULT" />
        </intent-filter>
</activity>
```


### (a) Use Bit6 UI Library

**Step 1.** In `layout.xml` file for `CallActivity`:

Add InCall UI component from Bit6 UI Library. It embeds video view, timer and controls.

```xml
<com.bit6.ui.InCallView
    android:id="@+id/incall_view"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent" />
```

**Step 2.** In `CallActivity.java`:

Initialize `InCallView` in `onCreate()` method

```java
InCallView inCallView = (InCallView) findViewById(R.id.incall_view);
inCallView.init(this, null);
```

Handle the intents notifying about new active calls in `onCreate()` and `onNewIntent()`

```java
// Get the call controller from the intent info
// 'intent' is an Intent with the call information
RtcDialog d = bit6.getCallClient().getDialogFromIntent(intent);
// Add the call to the rendered output
inCallView.addCall(d);
// Get updates about this call. For example, when it ends.
d.addStateListener(this);
```



### (b) Custom In-Call UI

**Step 1.** In `layout.xml` file for `CallActivity`:

Add MediaView component for rendering video streams

```xml
<com.bit6.sdk.ui.RtcMediaView android:id="@+id/media_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
<!-- Add your own UI for controlling the calls -->
```

**Step 2.** In `CallActivity.java`:

Initialize `MediaView` in `onCreate()` method

```java
RtcMediaView mediaView = (RtcMediaView)findViewById(R.id.media_view);
mediaView.init(bit6, this);
```

Your container view can contain any other views and controls like Button (disconnect, change scaling, switch camera), TextView (timer, destination name) and so on.

Handle the intents notifying about new active calls in `onCreate()` and `onNewIntent()`:

```java
// Get the call controller from the intent info
// 'intent' is an Intent with the call information
RtcDialog d = bit6.getCallClient().getDialogFromIntent(intent);
// Add the call to the rendered output
mediaView.addCall(d);
// Get updates about this call. For example, when it ends.
d.addStateListener(this);
```
