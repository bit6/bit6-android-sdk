
package com.bit6.samples.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.Ringer;
import com.bit6.sdk.RtcDialog;

public class IncomingCallActivity extends Activity implements
        RtcDialog.StateListener, OnClickListener {

    private RtcDialog dialog;
    private Button answer, reject;
    private Ringer ringer;
    private Bit6 bit6;
    private String callerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        int flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

        getWindow().setFlags(flags, flags);

        // Bit6
        bit6 = Bit6.getInstance();

        // Call controller RtcDialog
        dialog = bit6.getCallClient().getDialogFromIntent(getIntent());

        dialog.addStateListener(this);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(dialog.hasVideo() ? R.string.incoming_video_call
                : R.string.incoming_voice_call);

        // Address of the other party
        String other = dialog.getOther();

        // Get the username from 'usr:' uri
        int pos = other.indexOf(':');
        callerName = pos > 0 ? other.substring(pos + 1) : other;

        // Prepare the UI
        TextView message = (TextView) findViewById(R.id.message);
        String msg = String.format(getString(R.string.user_is_calling), callerName);
        message.setText(msg);

        // Answer call button
        answer = (Button) findViewById(R.id.answer);
        answer.setOnClickListener(this);
        // Reject call button
        reject = (Button) findViewById(R.id.reject);
        reject.setOnClickListener(this);

        // Utility class for playing a ringing tone
        ringer = new Ringer(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ringer.playRinging();
    }

    @Override
    protected void onStop() {
        ringer.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        dialog.removeStateListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        // Click on 'answer' or 'reject' button
        if (v == answer || v == reject) {
            // Stop incoming call ringing
            ringer.stop();
            // User answered the call
            if (v == answer) {
                // Launch default InCall Activity
                // dialog.launchInCallActivity(this);

                // Launch custom InCall Activity
                Intent intent = new Intent(this, CallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                dialog.setAsIntentExtra(intent);
                startActivity(intent);
            }
            // User rejected the call
            else {
                dialog.hangup();
            }
            // Close this activity
            finish();
        }
    }

    @Override
    public void onStateChanged(RtcDialog d, int state) {
        if (state == RtcDialog.MISSED) {
            String text = getString(R.string.missed_call);
            IncomingMessageReceiver.showNotification(this, text, dialog.getOther(), callerName);
        }
        else if (state == RtcDialog.END) {
            finish();
        }
    }
}
