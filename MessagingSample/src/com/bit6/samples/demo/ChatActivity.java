package com.bit6.samples.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.Message;
import com.bit6.sdk.Message.Messages;
import com.bit6.sdk.MessageStatusListener;
import com.bit6.sdk.ResultCallback;
import com.bit6.sdk.RtNotificationListener;
import com.bit6.sdk.RtcDialog;

public class ChatActivity extends Activity implements RtNotificationListener, MessageStatusListener {

	private String dest;
	private TextView mDest;
	private EditText mContent;
	private Button mSend;
	private Bit6 bit6;
	private String other;
	private TextView mTyping;
	private ListView mListView;

	private ChatAdapter mAdapter;
	private Cursor mCursor;
	private DataSetObserver mAdapterObserver;
	private Address to;

	public static final String mImagesCacheDir = Environment
			.getExternalStorageDirectory().getPath()
			+ "/MessagingSample/Images/";

	public static final String mVideosCacheDir = Environment
			.getExternalStorageDirectory().getPath()
			+ "/MessagingSample/Videos/";

	private static final int FILE_SELECT_PHOTO_CODE = 1;
	private static final int REQUEST_VIDEO_CAPTURE = 4;
	private static final int REQUEST_PHOTO_CAPTURE = 5;

	private String imageFileName = mImagesCacheDir + "photo_capture.jpg";

	private String videoFileName = mVideosCacheDir + "video_capture.mp4";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		bit6 = Bit6.getInstance();

		mDest = (TextView) findViewById(R.id.dest);

		if (getIntent().getExtras() == null) {
			return;
		} else {
			dest = getIntent().getExtras().getString("dest");
			other = dest;

			if (dest == null) {
				return;
			}

			if (dest.contains(":")) {
				to = Address.parse(dest);
			} else {
				to = Address.fromParts(Address.KIND_USERNAME, dest);
			}
			if(to != null){
				mCursor = bit6.getConversation(to);
			}			

			mListView = (ListView) findViewById(R.id.list);
			mAdapter = new ChatAdapter(this, mCursor, true);

			mAdapterObserver = new DataSetObserver() {
				@Override
				public void onChanged() {
					super.onChanged();
					scrollToNewestItem();
				}
			};

			mAdapter.registerDataSetObserver(mAdapterObserver);

			mListView.setAdapter(mAdapter);
			scrollToNewestItem();

			// User names are saved like usr:userName, so deleting that part of
			// user names
			if (other.contains(":")) {
				other = other.substring(other.indexOf(':') + 1);
			}
			mDest.setText(other);
			registerForContextMenu(mListView);

			mListView
					.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

						@Override
						public void onCreateContextMenu(ContextMenu menu,
								View v, ContextMenuInfo menuInfo) {
							AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
							Cursor c = (Cursor) mAdapter.getItem(info.position);
							String msgId = c.getString(c
									.getColumnIndex(Messages.ID));
							int messageId = -1;
							if (msgId != null) {
								messageId = Integer.parseInt(msgId);
							}
							menu.add(0, messageId, 0,
									getString(R.string.delete_message));
						}
					});
		}

		mContent = (EditText) findViewById(R.id.text);
		mTyping = (TextView) findViewById(R.id.typing);

		mContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (count == 0) {
					return;
				}
				Address address = Address.parse(dest);
				if (address != null) {
					bit6.sendTypingNotification(address);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		mSend = (Button) findViewById(R.id.send);
		mSend.setOnClickListener(mOnSendClick);

		bit6.addRtNotificationListener(this);
	}

	
	private void scrollToNewestItem() {
		// Scroll to the end of the list
		int pos = mListView.getCount() - 1;
		if (pos >= 0) {
			mListView.setSelection(pos);
		}
	}

	private OnClickListener mOnSendClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String content = mContent.getText().toString();

			if (to == null || TextUtils.isEmpty(content.trim())) {
				return;
			}			
			Message m =  Message.newMessage(to).text(content);
			bit6.sendMessage(m, ChatActivity.this);
			mContent.setText("");
		}
	};	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mAdapterObserver);
		}
		bit6.removeRtNotificationListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.conversation_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_video_call:
			startCall(true);
			break;
		case R.id.menu_voice_call:
			startCall(false);
			break;
		case R.id.menu_take_photo:
			startPhotoCapture();
			break;
		case R.id.menu_select_image:
			showImageChooser();
			break;
		case R.id.menu_take_video:
			startVideoCapture();
			break;
		case R.id.menu_share_location:
			shareLocation();
			break;		
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTyping(JSONObject json) {
		String from = json.optString("from");
		if (dest.equalsIgnoreCase(from)) {
			mTyping.setVisibility(View.VISIBLE);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					mTyping.setVisibility(View.INVISIBLE);
				}
			}, 1000);
		}
		Log.d("ChatActivity.onTyping()", "" + json.toString());
	}

	@Override
	public void onMessageUpdate(JSONObject json) {
		Log.d("ChatActivity.onMessageUpdate()", "" + json.toString());
	}

	@Override
	public void onNewMessage(JSONObject json) {
		Log.d("ChatActivity.onNewMessage()", "" + json.toString());
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		bit6.deleteMessage("" + item.getItemId(), new ResultCallback() {
			
			@Override
			public void onResult(boolean success, String msg) {
				
			}
		});
		return super.onContextItemSelected(item);
	}	
	
	private void startCall(boolean isVideo) {
		RtcDialog d = bit6.startCall(to, isVideo);
		d.launchInCallActivity(this);
	}
	
	private void startPhotoCapture() {

		File dir = new File(mImagesCacheDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Uri imageUri = Uri.fromFile(new File(imageFileName));
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, REQUEST_PHOTO_CAPTURE);
	}

	private void showImageChooser() {

		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

		try {
			startActivityForResult(
					Intent.createChooser(i, "Select a File to Upload"),
					FILE_SELECT_PHOTO_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
		}
	}

	private void startVideoCapture() {
		File dir = new File(mVideosCacheDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File file = new File(videoFileName);
		if (file.exists()) {
			file.delete();
		}

		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		Uri imageUri = Uri.fromFile(new File(videoFileName));
		takeVideoIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
				imageUri);
		takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 10485760L);// 10MB
		if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			Uri uri = null;

			switch (requestCode) {
			case FILE_SELECT_PHOTO_CODE:
				uri = data.getData();

				sendPhoto(getPathFromUri(uri));
				break;
			case REQUEST_PHOTO_CAPTURE:
				uri = Uri.fromFile(new File(imageFileName));
				try {
					InputStream inputStream = getContentResolver()
							.openInputStream(uri);
					Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					sendPhoto(createTempFileFromBitmap(bitmap));
				} catch (FileNotFoundException e) {
					Log.e("onActivityResult", e.getMessage());
				}
				break;

			case REQUEST_VIDEO_CAPTURE:
				uri = Uri.fromFile(new File(videoFileName));

				try {
					InputStream inputStream = getContentResolver()
							.openInputStream(uri);

					sendVideo(createVideoFileFromStream(inputStream));
				} catch (Exception e) {
					Log.e("onActivityResult", e.getMessage());
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private byte[] readBytes(InputStream inputStream) throws IOException {
		// this dynamically extends to take the bytes you read
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		// this is storage overwritten on each iteration with bytes
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		// we need to know how may bytes were read to write them to the
		// byteBuffer
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}

		// and then we can return your byte array.
		return byteBuffer.toByteArray();
	}

	private String createVideoFileFromStream(InputStream inputStream) {
		String dir = Environment.getExternalStorageDirectory().toString()
				+ "/TMPFOLDER/";
		String path = dir + "video.mp4";
		File f = new File(dir);
		if (!f.exists()) {
			f.mkdirs();
		}
		try {
			FileOutputStream out = new FileOutputStream(path);
			out.write(readBytes(inputStream));
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.e("createVideoFileFromStream", e.getMessage());
			path = null;
		}
		return path;
	}

	private String getPathFromUri(Uri uri) {
		String path = null;
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = getContentResolver().query(uri, projection, null,
						null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					path = cursor.getString(column_index);

				}

			} catch (Exception e) {
				Log.e("onActivityResult", e.toString());
			}
		}
		return path;
	}

	private String createTempFileFromBitmap(Bitmap scaledBitmap) {
		String strMyImagePath = null;
		String extr = Environment.getExternalStorageDirectory().toString();
		File mFolder = new File(extr + "/temp");
		if (!mFolder.exists()) {
			mFolder.mkdirs();
		}

		String s = "image.png";
		File f = new File(mFolder.getAbsolutePath(), s);
		if (f.exists()) {
			f.delete();
		}

		strMyImagePath = f.getAbsolutePath();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e("createTempFileFromBitmap()", e.getMessage());
		} catch (Exception e) {
			Log.e("createTempFileFromBitmap()", e.getMessage());
		}
		scaledBitmap.recycle();

		return strMyImagePath;
	}

	private void sendPhoto(String attachmentPath) {
		Message m = Message.newMessage(to).text("Your message here").photo(attachmentPath);
		bit6.sendMessage(m, this);
	}

	private void sendVideo(String attachmentPath) {
		Message m = Message.newMessage(to).text("Your message here").video(attachmentPath);
		bit6.sendMessage(m, this);
	}
	
	private void shareLocation(){
		//Message m = Message.newMessage(to).geoLocation(40.192324, 44.504161);
		//bit6.sendMessage(m, this);
		bit6.sendMyCurrentLocation(to, this);
	}

	@Override
	public void onResult(boolean success, String msg) {
		Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_LONG).show();
	}


	@Override
	public void onMessageStatusChanged(Message m, int state) {
		if(state == Message.STATUS_PREPARING){
			Toast.makeText(ChatActivity.this, "prepare", Toast.LENGTH_LONG)
			.show();
		}
	}	

}
