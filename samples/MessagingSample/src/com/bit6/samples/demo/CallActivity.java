package com.bit6.samples.demo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.RtcDialog.StateListener;
import com.bit6.sdk.ui.RtcMediaView;

public class CallActivity extends Activity implements StateListener {

	private Bit6 bit6;
	private RelativeLayout rootView;
	private View menuBar;
	private RtcMediaView rtcMediaView;
	private ImageButton videoScalingButton;
	private boolean scaleAspectFill;
	private RtcDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		rootView = (RelativeLayout) findViewById(R.id.root);
		menuBar = findViewById(R.id.menubar_fragment);
		bit6 = Bit6.getInstance();

		dialog = bit6.getDialogFromIntent(getIntent());
		dialog.addStateListener(this);

		rtcMediaView = dialog.createRtcMediaView(this);
		rootView.addView(rtcMediaView);
		menuBar.bringToFront();
		rootView.invalidate();

		videoScalingButton = (ImageButton) findViewById(R.id.button_scaling_mode);

		videoScalingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (scaleAspectFill) {
					videoScalingButton
							.setBackgroundResource(R.drawable.ic_action_full_screen);
					scaleAspectFill = false;
					rtcMediaView.setScaleFill(scaleAspectFill);

				} else {
					videoScalingButton
							.setBackgroundResource(R.drawable.ic_action_return_from_full_screen);
					scaleAspectFill = true;
					rtcMediaView.setScaleFill(scaleAspectFill);
				}
			}
		});

		rtcMediaView.setRemotVideoViewParams(0, 0, 100, 100, true);
		rtcMediaView.setLocalVideoViewParams(70, 0, 28, 28, false);

		((ImageButton) findViewById(R.id.button_disconnect))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						dialog.hangup();
						finish();
					}
				});

		ImageButton switchCameraButton = (ImageButton) findViewById(R.id.button_switch_camera);
		if (!dialog.hasVideo()) {
			switchCameraButton.setVisibility(View.GONE);
			videoScalingButton.setVisibility(View.GONE);
		}

		switchCameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				rtcMediaView.switchCamera();
			}
		});
	}

	public static class MenuBarFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fr_menubar, container, false);
		}
	}

	@Override
	public void onStateChanged(RtcDialog d, int state) {
		if (state == RtcDialog.END) {
			finish();
		}
	}

}
