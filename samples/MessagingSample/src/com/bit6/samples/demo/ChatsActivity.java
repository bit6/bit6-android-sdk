
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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        bit6 = Bit6.getInstance();

        // Cursor with messages grouped by chat members user names
        // Each row contains:
        // Messages._ID, Messages.OTHER, Messages.CONTENT, Messages.CREATED
        cursor = bit6.getMessageClient().getConversations();

        // Scroll to the beginning of the list when data changes
        mAdapterObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                scrollToNewestItem();
            }
        };

        // Conversations
        mAdapter = new ChatsAdapter(this, cursor, true);
        mAdapter.registerDataSetObserver(mAdapterObserver);

        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                String other = c.getString(c.getColumnIndex(Messages.OTHER));
                showChatActivity(other);
            }
        });

        // User to start a new conversation with
        mDest = (EditText) findViewById(R.id.dest);
        hideKeyboard(mDest);

        // Start a new conversation
        mCompose = (Button) findViewById(R.id.compose);
        mCompose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String dest = mDest.getText().toString().trim();
                if (!TextUtils.isEmpty(dest)) {
                    showChatActivity(dest);
                }
            }
        });

        // Logout button
        mLogout = (TextView) findViewById(R.id.logout);
        mLogout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Logout and delete saved data
                bit6.getSessionClient().logout();
                // Go back to login screen
                Intent intent = new Intent(ChatsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Show current user name
        String txt = getString(R.string.logged_in, bit6.getSessionClient().getOwnIdentity().toString());
        TextView tv = (TextView) findViewById(R.id.username);
        tv.setText(txt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.unregisterDataSetObserver(mAdapterObserver);
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
                showPstnCallDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Show Chat activity
    private void showChatActivity(String other) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("dest", other);
        startActivity(intent);
    }

    // Show UI for making a phone call
    private void showPstnCallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_pstn_call, null);
        final EditText phoneNumber = (EditText) layout.findViewById(R.id.phone_number);

        builder.setView(layout)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);
        builder.setTitle(R.string.pstn_dialog_title);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String phone = phoneNumber.getText().toString();
                int len = phone != null ? phone.length() : 0;
                // Is phone number valid?
                if (len > 6 && len < 14 && phone.charAt(0) == '+') {
                    RtcDialog d = bit6.getCallClient().startPhoneCall(phone);
                    d.launchInCallActivity(ChatsActivity.this);
                    dialog.dismiss();
                } else {
                    Toast.makeText(ChatsActivity.this,
                            R.string.invalid_phone_number,
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Show the dialog
        dialog.show();
    }

    // Scroll to the start of list
    private void scrollToNewestItem() {
        mListView.setSelection(0);
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
