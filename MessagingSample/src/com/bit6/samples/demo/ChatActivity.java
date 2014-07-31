package com.bit6.samples.demo;

import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import com.bit6.sdk.Message.Messages;
import com.bit6.sdk.MessageListener;
import com.bit6.sdk.OnResponseReceived;

public class ChatActivity extends Activity implements MessageListener {

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

			mCursor = bit6.getConversation(to.toString());

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
		bit6.addMessageListener(this);
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
			bit6.sendMessage(to, content, new OnResponseReceived() {

				@Override
				public void onResponse(boolean success, String msg) {
					if (success) {
						Toast.makeText(ChatActivity.this, msg,
								Toast.LENGTH_LONG).show();
						mAdapter.notifyDataSetChanged();
						scrollToNewestItem();
					} else {
						Toast.makeText(ChatActivity.this, msg,
								Toast.LENGTH_LONG).show();
					}
				}
			});
			mContent.setText("");
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mAdapterObserver);
		}
		bit6.removeMessageListener(this);
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

		bit6.deleteMessage("" + item.getItemId(), new OnResponseReceived() {

			@Override
			public void onResponse(boolean success, String msg) {

			}
		});
		return super.onContextItemSelected(item);
	}

}
