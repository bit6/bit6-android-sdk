package com.bit6.samples.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.bit6.sdk.RtcDialog;

public class ChatsActivity extends Activity {

	private TextView mLogout;
	private Button mCompose;
	private Bit6 bit6;
	private EditText mDest;
	private ListView mListView;
	private Cursor cursor;
	private DataSetObserver mAdapterObserver;
	private ChatsAdapter mAdapter;

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
				if (!TextUtils.isEmpty(number) && number.lastIndexOf('+') == 0
						&& number.length() > 6 && number.length() < 14) {
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
	}

	private void scrollToNewestItem() {
		// Scroll to the start of list
		mListView.setSelection(0);
	}

}
