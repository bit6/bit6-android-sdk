package com.bit6.samples.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.Message.Messages;
import com.bit6.sdk.attachment.DownloadStateListener;

public class PhotoViewActivity extends Activity {

	private String messageId;
	private ProgressDialog mpProgressDialog;
	private ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_view);

		mImageView = (ImageView) findViewById(R.id.img_view);

		if (getIntent().getExtras() != null) {
			messageId = getIntent().getExtras().getString(Messages._ID);

			mpProgressDialog = new ProgressDialog(this);
			mpProgressDialog
					.setTitle(getString(R.string.image_viewer_please_wait));
			mpProgressDialog
					.setMessage(getString(R.string.image_viewer_loading_image));
			mpProgressDialog.setCanceledOnTouchOutside(false);

			Bit6.getInstance().loadAttachment(messageId,
					new DownloadStateListener() {

						@Override
						public void onDownloadStarted() {
							showProgressDialog(true);
						}

						@Override
						public void onDownloadFinished(String result) {
							showProgressDialog(false);
							mImageView.setImageBitmap(BitmapFactory.decodeFile(result));							
						}
						
						@Override
						public void onDownloadFailed() {
							showProgressDialog(false);
							Toast.makeText(PhotoViewActivity.this, "Failed to download", Toast.LENGTH_LONG).show();
							finish();
						}
					});
		}
	}

	private void showProgressDialog(boolean show) {
		if (mpProgressDialog == null) {
			return;
		}
		if (show) {
			mpProgressDialog.show();
		} else {
			mpProgressDialog.dismiss();
		}
	}

	@Override
	protected void onPause() {
		showProgressDialog(false);
		super.onPause();
	}

}
