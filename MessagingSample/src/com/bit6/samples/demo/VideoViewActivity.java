package com.bit6.samples.demo;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.Message.Messages;
import com.bit6.sdk.attachment.DownloadStateListener;

public class VideoViewActivity extends Activity {
	private String messageId;
	private ProgressDialog mpProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent().getExtras() != null) {
			messageId = getIntent().getExtras().getString(Messages._ID);
			mpProgressDialog = new ProgressDialog(this);
			mpProgressDialog
					.setTitle(getString(R.string.image_viewer_please_wait));
			mpProgressDialog
					.setMessage(getString(R.string.video_viewer_loading_image));
			mpProgressDialog.setCanceledOnTouchOutside(false);

			Bit6.getInstance().loadAttachment(messageId,
					new DownloadStateListener() {

						@Override
						public void onDownloadStarted() {
							showProgressDialog(true);
						}

						@Override
						public void onDownloadFinished(String filePath) {
							showProgressDialog(false);
							File file = new File(filePath);
							String extension = android.webkit.MimeTypeMap
									.getFileExtensionFromUrl(Uri.fromFile(file)
											.toString());
							String mimetype = android.webkit.MimeTypeMap
									.getSingleton().getMimeTypeFromExtension(
											extension);

							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(file), mimetype);
							startActivity(intent);
							finish();
						}

						@Override
						public void onDownloadFailed() {
							showProgressDialog(false);
							Toast.makeText(VideoViewActivity.this,
									"Failed to download", Toast.LENGTH_LONG)
									.show();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}

}
