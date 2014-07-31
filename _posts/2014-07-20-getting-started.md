### Add Bit6 SDK to your Eclipse project

1. [Download](https://github.com/bit6/bit6-android-sdk/) the Bit6 SDK.

2. Add the Bit6 SDK to your application's libs folder.

3. Add Bit6 as a library from properties -> Android, click OK.

<img style="max-width:100%" src="images/project_properties.png"/>

### Get a Bit6 API Key

You will need an API key in order to initialize and use Bit6 SDK. Send an email to dev@bit6.com to receive an application key.

### Setup Application class

In your Application class

**Step 1.** Import Bit6: `import com.bit6.sdk.Bit6;`

**Step 2.** Add the following to `onCreate()` method:

```java
Bit6.getInstance().init(getApplicationContext(), apikey, this, gcmSenderId);
```

