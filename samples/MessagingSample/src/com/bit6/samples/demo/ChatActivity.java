
package com.bit6.samples.demo;

import java.io.File;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
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

import com.bit6.samples.demo.imagecache.ImageCache;
import com.bit6.samples.demo.imagecache.ImageFetcher;
import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.Group;
import com.bit6.sdk.Message;
import com.bit6.sdk.MessageStatusListener;
import com.bit6.sdk.NotificationClient;
import com.bit6.sdk.OutgoingMessage;
import com.bit6.sdk.ResultHandler;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.db.Contract;

public class ChatActivity extends Activity implements NotificationClient.Listener,
        MessageStatusListener {

    private static final String TAG = "ChatActivity";

    private static final int
            REQUEST_SELECT_IMAGE = 1,
            REQUEST_VIDEO_CAPTURE = 4,
            REQUEST_PHOTO_CAPTURE = 5;

    public final static String INTENT_EXTRA_DEST = "dest";
    public final static String INTENT_EXTRA_CONV_ID = "conv_id";

    private Bit6 bit6;
    private Address other;
    // Conversation id
    private String c_id;

    private EditText mContent;
    private Button mSend;
    private TextView mTyping;
    private ListView mListView;

    private ChatAdapter mAdapter;
    private Cursor mCursor;
    private DataSetObserver mAdapterObserver;

    private String
            tmpImageFileName = "photo_capture.jpg",
            tmpVideoFileName = "video_capture.mp4";

    private ImageFetcher mImageFetcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Use ImageCache from the excellent Android example
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, "thumbs");
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(this, 180);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(null, cacheParams);

        // Bit6 instance
        bit6 = Bit6.getInstance();

        // Message compose text field
        mContent = (EditText) findViewById(R.id.text);
        mContent.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    bit6.getNotificationClient().sendTypingNotification(other);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Message send button
        mSend = (Button) findViewById(R.id.send);
        mSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });

        // Typing notification area
        mTyping = (TextView) findViewById(R.id.typing);

        // Listen to 'typing' notifications
        bit6.getNotificationClient().addListener(this);

        onNewIntent(getIntent());
        Group g = Group.newGroup();
        g.setPermission(Group.VIEW, Group.ROLE_ALL);
        g.setPermission(Group.MEM_LIST, Group.ROLE_ADMIN);
        g.setPermission(Group.EDIT, Group.ROLE_USER);
        bit6.createGroup(g, ResultHandler.EMPTY);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        showConversation(intent.getStringExtra(INTENT_EXTRA_DEST), intent.getStringExtra(INTENT_EXTRA_CONV_ID));
    }

    @Override
    protected void onDestroy() {
        bit6.getNotificationClient().removeListener(this);
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mAdapterObserver);
        }
        super.onDestroy();
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
            case R.id.menu_take_video:
                startVideoCapture();
                break;
            case R.id.menu_select_image:
                showImageChooser();
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
    public boolean onContextItemSelected(MenuItem item) {
        int position = item.getItemId();
        Cursor c = (Cursor) mAdapter.getItem(position);
        String msgId = c.getString(c.getColumnIndex(Contract.Messages._ID));
        bit6.getMessageClient().deleteMessage(msgId, new ResultHandler() {

            @Override
            public void onResult(boolean success, String msg) {
                if (msg != null) {
                    Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });

        return super.onContextItemSelected(item);
    }

    @Override
    public void onTypingReceived(String from) {
        if (other.toString().equalsIgnoreCase(from)) {
            mTyping.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mTyping.setVisibility(View.INVISIBLE);
                }
            }, 1000);
        }
    }

    @Override
    public void onNotificationReceived(String from, String type, JSONObject data) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnectedChanged(boolean isConnected) {
        // TODO Auto-generated method stub
    }

    
    // Start voice or video call
    private void startCall(boolean isVideo) {
        RtcDialog dialog = bit6.getCallClient().startCall(other, isVideo);

        // Launch default InCall Activity
        // d.launchInCallActivity(this);

        // Launch custom InCall Activity
        Intent intent = new Intent(this, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        dialog.setAsIntentExtra(intent);
        startActivity(intent);
    }

    private void startPhotoCapture() {
        // Save to this file
        Uri output = getCacheUri(tmpImageFileName);
        if (output == null)
            return;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        startActivityForResult(intent, REQUEST_PHOTO_CAPTURE);
    }

    private void startVideoCapture() {
        // Save to this file
        Uri output = getCacheUri(tmpVideoFileName);
        if (output == null)
            return;

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 10L * 1024L * 1024L); // 10MB
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        }
    }

    // Get URI for a cached file
    private Uri getCacheUri(String fname) {
        File dir = getExternalCacheDir();
        if (dir == null) {
            Log.e(TAG, "Cannot get external cache dir");
            return null;
        }
        return Uri.fromFile(new File(dir, fname));
    }

    private void showImageChooser() {
        Intent target = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        Intent i = Intent.createChooser(target, "Select a Photo to Send");

        try {
            startActivityForResult(i, REQUEST_SELECT_IMAGE);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File dir = getExternalCacheDir();
        if (dir == null) {
            Log.e(TAG, "Cannot get external cache dir");
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_IMAGE:
                    Uri uri = data.getData();
                    sendMessageWithFile("Selected photo", new File(getImageFilePathFromUri(uri)));
                    break;
                case REQUEST_PHOTO_CAPTURE:
                    sendMessageWithFile("New photo", new File(dir, tmpImageFileName));
                    break;
                case REQUEST_VIDEO_CAPTURE:
                    sendMessageWithFile("New video", new File(dir, tmpVideoFileName));
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Convert a URI of the selected image to a filePath
    // May not be the perfect solution
    // http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app
    private String getImageFilePathFromUri(Uri uri) {
        String[] filePathColumn = {
                MediaStore.Images.Media.DATA
        };

        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    // Send a text message
    private void sendTextMessage() {
        String text = mContent.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            mContent.setText("");
            OutgoingMessage m = bit6.getMessageClient().compose(other).text(text);
            m.send(this);
        }
    }

    // Send a message with attachment
    private void sendMessageWithFile(String text, File f) {
        OutgoingMessage m = bit6.getMessageClient().compose(other).text(text)
                .attach(f.getAbsolutePath());
        m.send(this);
    }

    // Send your current location
    private void shareLocation() {
        bit6.getMessageClient().sendMyCurrentLocation(other, this);
    }

    @Override
    public void onMessageStatusChanged(Message m, int state) {
        if (state == Contract.Messages.STATUS_PREPARING) {
            Toast.makeText(ChatActivity.this, "prepare", Toast.LENGTH_LONG).show();
        }
    }

    private void showConversation(String convId, final String conv_id) {

        // Create Bit6 Address object from a destination
        if (convId != null) {
            other = Address.parse(convId);
        }

        // We do not have a valid address!
        if (other == null) {
            Toast.makeText(this, "No valid address in ChatActivity", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        if(TextUtils.isEmpty(conv_id)){
            Cursor c = getContentResolver().query(
                    Contract.Conversations.CONTENT_URI, null,
                    Contract.Conversations.ID+"=?", new String[]{convId}, null);
            while(c.moveToNext()){
                c_id = c.getString(c.getColumnIndex(Contract.Conversations._ID));
            }
        }else{
            c_id = conv_id;
        }

        bit6.getMessageClient().markConversationAsRead(c_id);

        // Show user we are chatting with
        TextView tv = (TextView) findViewById(R.id.dest);
        tv.setText(other.getValue());

        // Cursor to messages in this conversion
        mCursor = getContentResolver().query(
                Contract.Messages.CONTENT_URI, null,
                Contract.Messages.CONVERSATION_ID + "=?", new String[] {
                    c_id
                },
                Contract.Messages.CREATED + " ASC");

        mAdapterObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                scrollToNewestItem();
                bit6.getMessageClient().markConversationAsRead(c_id);
            }
        };

        mAdapter = new ChatAdapter(this, mCursor, mImageFetcher, true);
        mAdapter.registerDataSetObserver(mAdapterObserver);

        // List of messages
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mAdapter);

        // Context menu for the list
        registerForContextMenu(mListView);
        mListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                menu.add(0, info.position, 0, getString(R.string.delete_message));
            }
        });

        // Scroll to the latest message
        scrollToNewestItem();

    }

    // Scroll to the latest message
    private void scrollToNewestItem() {
        // Scroll to the end of the list
        int pos = mListView.getCount() - 1;
        if (pos >= 0) {
            mListView.setSelection(pos);
        }
    }
}
