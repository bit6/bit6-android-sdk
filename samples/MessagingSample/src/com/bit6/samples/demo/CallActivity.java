
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
    private RtcDialog dialog;
    private RtcMediaView rtcMediaView;
    private boolean scaleAspectFill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        bit6 = Bit6.getInstance();

        // Current call controller RtcDialog
        dialog = bit6.getCallClient().getDialogFromIntent(getIntent());
        dialog.addStateListener(this);

        // MediaView for displaying video streams
        rtcMediaView = dialog.createRtcMediaView(this);

        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.root);
        View menuBar = findViewById(R.id.menubar_fragment);
        rootView.addView(rtcMediaView);
        menuBar.bringToFront();
        rootView.invalidate();

        // Configure media view
        rtcMediaView.setRemoteVideoViewParams(0, 0, 100, 100, true);
        rtcMediaView.setLocalVideoViewParams(70, 0, 28, 28, false);

        // Hangup button
        View v = findViewById(R.id.button_disconnect);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hangup();
                finish();
            }
        });

        // Toggle video scaling
        ImageButton videoScalingButton = (ImageButton) findViewById(R.id.button_scaling_mode);
        // Switch camera button
        ImageButton switchCameraButton = (ImageButton) findViewById(R.id.button_switch_camera);

        // Video call
        if (dialog.hasVideo()) {
            switchCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rtcMediaView.switchCamera();
                }
            });
            videoScalingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toggle the mode
                    scaleAspectFill = !scaleAspectFill;
                    // Set the new scaling mode
                    rtcMediaView.setScaleFill(scaleAspectFill);
                    // Change the button icon
                    int resId = scaleAspectFill ? 
                            R.drawable.ic_action_return_from_full_screen : 
                            R.drawable.ic_action_full_screen;
                    view.setBackgroundResource(resId);
                }
            });

        }
        // Audio call
        else {
            switchCameraButton.setVisibility(View.GONE);
            videoScalingButton.setVisibility(View.GONE);
        }

    }

    public static class MenuBarFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.incall_menubar, container, false);
        }
    }

    @Override
    public void onStateChanged(RtcDialog d, int state) {
        if (state == RtcDialog.END) {
            finish();
        }
    }

}
