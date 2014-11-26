---
category: multimedia messaging
title: 'Attachments'

layout: nil
---

### Send a Message with Attachment

```java
Message m = Message.newMessage(to).text("Text").photo(attachmentPath);
bit6.sendMessage(m, statusListener);
```

```java
Message m = Message.newMessage(to).text("Text").video(attachmentPath);
bit6.sendMessage(m, statusListener);
```

To follow the message status as it is being sent implement `MessageStatusListener`.

```java
import com.bit6.sdk.MessageStatusListener;

@Override
public void onMessageStatusChanged(Message m, int state) {
	if (state == Message.STATUS_PREPARING) {

	} else if (state == Message.STATUS_UPLOADED) {	

	} else if (state == Message.STATUS_SENDING) {

	} else if (state == Message.STATUS_FAILED) {

	}
}
```


### Send Location Message 

To share current location call `bit6.sendMyCurrentLocation(to, statusListener);`

To send location message with text, latitude and longitude create a message object and set data.

```java
Message m = Message.newMessage(to).geoLocation(40.192324, 44.504161);
bit6.sendMessage(m, statusListener);
``` 

### Show Attachment Thumbnails

Get message flags from cursor:

```java
int flags = cursor.getInt(cursor.getColumnIndex(Messages.FLAGS));
```

Check if the message contains attachment or location, get bitmap from given path and set to your image view. 

```java
if (Message.isAttachment(flags) || Message.isGeoLocation(flags)) {
    String path = cursor.getString(cursor.getColumnIndex(Messages.THUMB_PATH));
    Bitmap bitmap = BitmapFactory.decodeFile(path);
}
``` 

To download thumbnail for a specific message:

```java
String messageId = cursor.getString(cursor.getColumnIndex(Messages._ID));
bit6.downloadThumbForMessage(messageId);
``` 

To get attachment type from message:

```java
String type = Message.getAttachmentType(cursor.getString(
                   cursor.getColumnIndex(Messages.DATA)));
``` 

To check if message is incoming or outgoing:

```java
boolean isIncoming = Message.isIncoming(flags)
```


### Download and View Attachment 
To download the attachment data: 

```java
bit6.loadAttachment(messageId, new DownloadStateListener() {

	public void onDownloadStarted() {
	    showProgressDialog(true);
	}

	public void onDownloadFinished(String result) {
		showProgressDialog(false);
		mImageView.setImageBitmap(BitmapFactory.decodeFile(result));
	}

	public void onDownloadFailed() {
	    showProgressDialog(false);
	    Toast.makeText(PhotoViewActivity.this,"Failed to download", Toast.LENGTH_LONG).show();
	    finish();
	}
});
``` 

To open sent or received location call `message.getMapUri()`

```java
Message message = Message.createMessage(cursor);
Uri uri = message.getMapUri();
Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
startActivity(intent);
```
