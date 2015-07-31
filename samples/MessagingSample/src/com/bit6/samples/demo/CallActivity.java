
package com.bit6.samples.demo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bit6.sdk.Address;
import com.bit6.sdk.Bit6;
import com.bit6.sdk.RtcDialog;
import com.bit6.sdk.RtcDialog.StateListener;
import com.bit6.sdk.db.Contract.Conversations;
import com.bit6.sdk.ui.RtcMediaView;

public class CallActivity extends Activity implements StateListener {

    private Bit6 bit6;
    private RtcDialog dialog;
    private RtcMediaView rtcMediaView;
    private boolean scaleAspectFill;
    private List<String> contacts = new ArrayList<String>();
    private Dialog alertDialog;
    private boolean useVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        bit6 = Bit6.getInstance();

        // Current call controller RtcDialog
        dialog = bit6.getCallClient().getDialogFromIntent(getIntent());
        dialog.addStateListener(this);
        useVideo = dialog.hasVideo();

        // MediaView for displaying video streams
        rtcMediaView = new RtcMediaView(bit6, this);

        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.root);
        View menuBar = findViewById(R.id.menubar_fragment);
        rootView.addView(rtcMediaView);
        menuBar.bringToFront();
        rootView.invalidate();

        // Configure media view
        // rtcMediaView.setRemoteVideoViewParams(0, 0, 100, 100, true);
        // rtcMediaView.setLocalVideoViewParams(70, 0, 28, 28, false);

        // Hangup button
        View v = findViewById(R.id.button_disconnect);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bit6.getCallClient().hangupAll();
            }
        });

        // Toggle video scaling
        ImageButton videoScalingButton = (ImageButton) findViewById(R.id.button_scaling_mode);
        // Switch camera button
        ImageButton switchCameraButton = (ImageButton) findViewById(R.id.button_switch_camera);

        ImageButton addPerson = (ImageButton) findViewById(R.id.add_person);
        addPerson.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showContactList();
            }
        });

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
        getContactList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        RtcDialog dialog = bit6.getCallClient().getDialogFromIntent(intent);
        showIncomingCallDialog(dialog);
        super.onNewIntent(intent);
    }

    private void getContactList() {
        Cursor cursor = getContentResolver().query(Conversations.CONTENT_URI, new String[] {
                Conversations.ID
        }, null, null, null);
        while (cursor.moveToNext()) {
            String contactName = cursor.getString(cursor.getColumnIndex(Conversations.ID));
            if (dialog != null && !contactName.equals(dialog.getOther()))
                contacts.add(contactName);
        }
    }

    public static class MenuBarFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.incall_menubar, container, false);
        }
    }

    @Override
    public void onStateChanged(RtcDialog d, int state) {
        if (state == RtcDialog.END) {
            if(bit6.getCallClient().getRtcDialogs().size() == 0){
                finish();
            }else{
                if(!contacts.contains(d.getOther()))
                contacts.add(d.getOther());
            }
            
        }
    }

    private void showContactList() {
        
        List<RtcDialog> rtcDialogs = bit6.getCallClient().getRtcDialogs();
        for(RtcDialog rtcDialog : rtcDialogs){
            String contactname = rtcDialog.getOther();
            if(contacts.contains(contactname)){
                contacts.remove(contactname);
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_contact);

        ListView modeList = new ListView(this);
        String[] stringArray = new String[contacts.size()];
        contacts.toArray(stringArray);
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        modeList.setAdapter(modeAdapter);
        modeList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dest = (String) parent.getItemAtPosition(position);
                RtcDialog dialog = bit6.getCallClient().startCall(Address.parse(dest), useVideo);
                dialog.addStateListener(CallActivity.this);
                rtcMediaView.addRtcDialog(dialog);
                alertDialog.dismiss();
            }
        });

        builder.setView(modeList);
        alertDialog = builder.create();

        alertDialog.show();
    }

    private void showIncomingCallDialog(final RtcDialog rtcDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setTitle("Incoming call from " + rtcDialog.getOther())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int id) {
                        rtcDialog.addStateListener(CallActivity.this);
                        rtcMediaView.addRtcDialog(rtcDialog);
                        d.dismiss();
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int id) {
                rtcDialog.hangup();
                d.dismiss();
            }
        });
        // Create the AlertDialog
        AlertDialog d = builder.create();
        d.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        rtcMediaView.updateVideoView();
        super.onConfigurationChanged(newConfig);
    }

}
