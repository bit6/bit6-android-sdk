package com.bit6.samples.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.Call;
import com.bit6.sdk.CallStateListener;
import com.bit6.sdk.Ringer;
import com.bit6.sdk.WakeLocker;

public class IncomingCallActivity extends Activity implements CallStateListener {

	private Call call;
	private Button answer, reject;
	private Ringer ringer;
	private Bit6 bit6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_incoming_call);

		int flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

		getWindow().setFlags(flags, flags);

		ringer = new Ringer(this);
		bit6 = Bit6.getInstance();
		bit6.registerCallStateListener(this);
		call = getIntent().getExtras().getParcelable("call");		
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(call.isVideo() ? R.string.incoming_video_call : R.string.incoming_voice_call);

		String dest = call.getSender();

		String callerName = dest.toString().substring(
				dest.toString().indexOf(":") + 1);
		String msg = String.format(getString(R.string.user_is_calling),
				callerName);

		TextView message = (TextView) findViewById(R.id.message);
		message.setText(msg);

		answer = (Button) findViewById(R.id.answer);
		reject = (Button) findViewById(R.id.reject);

		answer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onAnswerClick();
			}
		});

		reject.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onRejectClick();
			}
		});
	}

	private void onAnswerClick() {
		ringer.stop();
		bit6.answerCall(call);
		finish();
	}

	private void onRejectClick() {
		ringer.stop();
		bit6.rejectCall(call);
		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// TODO Maybe use bit6.acquireLocker(this) and bit.playRinging()?
		WakeLocker.acquire(this);
		ringer.playRinging();
	}

	@Override
	protected void onStop() {
		ringer.stop();
		WakeLocker.release();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		bit6.unregisterCallStateListener();
		super.onDestroy();
	}

	@Override
	public void onCallEnded() {
		finish();
	}

	@Override
	public void onCallFailed() {
		// TODO Auto-generated method stub
		
	}
}
