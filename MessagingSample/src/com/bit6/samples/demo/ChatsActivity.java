package com.bit6.samples.demo;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.Message.Messages;
import com.bit6.sdk.RtNotificationListener;
import com.bit6.sdk.RtcDialog;

public class ChatsActivity extends Activity implements RtNotificationListener {

	private TextView mLogout;
	private Button mCompose;
	private Bit6 bit6;
	private EditText mDest;
	private ListView mListView;
	private Cursor cursor;
	private DataSetObserver mAdapterObserver;
	private ChatsAdapter mAdapter;
	private NotificationManager mNotificationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_list);

		bit6 = Bit6.getInstance();

		// Cursor object which contains messages grouped by chat members user
		// names
		// Each row contains Messages._ID,
		// Messages.OTHER, Messages.CONTENT, Messages.CREATED by specified
		// order.
		cursor = bit6.getConversations();

		String message = getString(R.string.loged_in);
		TextView tv = (TextView) findViewById(R.id.username);
		tv.setText(message + " " + bit6.getOwnIdentity().toString() + " ");

		mListView = (ListView) findViewById(R.id.list);

		mAdapter = new ChatsAdapter(this, cursor, true);

		mAdapterObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				scrollToNewestItem();
			}
		};

		mAdapter.registerDataSetObserver(mAdapterObserver);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor c = (Cursor) parent.getItemAtPosition(position);
				String other = c.getString(c.getColumnIndex(Messages.OTHER));
				Intent intent = new Intent(ChatsActivity.this,
						ChatActivity.class);
				intent.putExtra("dest", other);
				startActivity(intent);
			}
		});

		mDest = (EditText) findViewById(R.id.dest);

		hideKeyboard(mDest, this);

		mLogout = (TextView) findViewById(R.id.logout);
		mLogout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Delete account's saved data from preferences
				bit6.logout();
				Intent intent = new Intent(ChatsActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		});

		mCompose = (Button) findViewById(R.id.compose);
		mCompose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String dest = mDest.getText().toString();
				if (!TextUtils.isEmpty(dest.trim())) {
					Intent intent = new Intent(ChatsActivity.this,
							ChatActivity.class);
					intent.putExtra("dest", dest);
					startActivity(intent);
				}
			}
		});
		bit6.addRtNotificationListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chats_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_pstn_call:
			showPSTNCallDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showPSTNCallDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.dialog_pstn_call, null);
		final EditText numberArea = (EditText) layout
				.findViewById(R.id.number_area);

		builder.setView(layout).setPositiveButton(R.string.ok, null)
				.setNegativeButton(R.string.cancel, null);
		builder.setTitle(R.string.pstn_dialog_title);

		final AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		positive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String number = numberArea.getText().toString();
				if (!TextUtils.isEmpty(number) && number.lastIndexOf('+') == 0 && number.length()>6 && number.length()<14) {
					RtcDialog d = bit6.startPhoneCall(number);
					d.launchInCallActivity(ChatsActivity.this);
					dialog.dismiss();
				} else {
					Toast.makeText(ChatsActivity.this,
							R.string.invalid_phone_number, Toast.LENGTH_LONG)
							.show();
				}
			}
		});
	}

	public static void hideKeyboard(View view, Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		// force close everything
		activity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdapter.unregisterDataSetObserver(mAdapterObserver);
		bit6.removeRtNotificationListener(this);
	}

	private void scrollToNewestItem() {
		// Scroll to the start of list
		mListView.setSelection(0);
	}

	@Override
	public void onTyping(JSONObject json) {
		Log.d("ChatsActivity.onTyping()", "" + json.toString());
	}

	@Override
	public void onMessageUpdate(JSONObject json) {
		Log.d("ChatsActivity.onMessageUpdate()", "" + json.toString());

	}

	@Override
	public void onNewMessage(JSONObject json) {
		Log.d("ChatsActivity.onNewMessage()", "" + json.toString());
		String content = json.optString("content");
		String sender = json.optString("sender");
		sendNotification(content, sender);
	}

	private void sendNotification(String msg, String sender) {

		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra("dest", sender);

		PendingIntent contentIntent = PendingIntent.getActivity(this,
				msg.hashCode(), intent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(sender)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		builder.setContentIntent(contentIntent);
		builder.setAutoCancel(true);
		mNotificationManager.notify(1, builder.build());
	}

}
