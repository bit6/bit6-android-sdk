package com.bit6.samples.demo;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bit6.sdk.Bit6;
import com.bit6.sdk.Message;
import com.bit6.sdk.Message.Messages;

public class ChatAdapter extends CursorAdapter {
	
	private Context mContext;
	private Bit6 bit6;
	private Cursor cursor;

	public ChatAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		this.mContext = context;
		this.cursor = c;
		bit6 = Bit6.getInstance();		
	}

	@Override
	public void bindView(View view,final Context context, Cursor cursor) {
		String content = cursor.getString(cursor
				.getColumnIndex(Messages.CONTENT));
		TextView contentTv = (TextView) view.findViewById(R.id.content);
		contentTv.setText(content);

		TextView statusTv = (TextView) view.findViewById(R.id.status);
		TextView dateTv = (TextView) view.findViewById(R.id.date);

		ImageView thumb = (ImageView) view.findViewById(R.id.thumb);
		ImageView video = (ImageView) view.findViewById(R.id.video_icon);
		
		thumb.setVisibility(View.GONE);
		video.setVisibility(View.GONE);

		long stamp = cursor.getLong(cursor.getColumnIndex(Messages.UPDATED));
		if (stamp == 0) {
			stamp = cursor.getLong(cursor.getColumnIndex(Messages.CREATED));
		}
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT);
		String stampString = df.format(new Date(stamp));

		dateTv.setText(stampString);

		int flags = cursor.getInt(cursor.getColumnIndex(Messages.FLAGS));

		if(Message.isAttachment(flags) || Message.isGeoLocation(flags)) {
			
			String thumbPath = cursor.getString(cursor
					.getColumnIndex(Messages.THUMB_PATH));
			String messageId = cursor.getString(cursor.getColumnIndex(Messages._ID));
			if(!TextUtils.isEmpty(thumbPath)){
				Bitmap bitmap = BitmapFactory.decodeFile(thumbPath);
				if(bitmap != null){
					thumb.setImageBitmap(bitmap);	
				}else{
					bit6.downloadThumbForMessage(messageId);
				}
				
				thumb.setVisibility(View.VISIBLE);
				
				String type = Message.getAttachmentType(cursor.getString(cursor.getColumnIndex(Messages.DATA)));
				if(Message.isVideoAttachment(type)){
					video.setVisibility(View.VISIBLE);
				}else{
					video.setVisibility(View.GONE);
				}
				thumb.setOnClickListener(new onThumbnailClickListener(messageId, type, cursor.getPosition()));
			}else{
				thumb.setImageBitmap(null);
				thumb.setVisibility(View.GONE);
				video.setVisibility(View.GONE);
				bit6.downloadThumbForMessage(messageId);
			}
			
		} else {
			thumb.setVisibility(View.GONE);
		}

		if (Message.isIncoming(flags)) {
			statusTv.setVisibility(View.GONE);
			view.setBackgroundColor(context.getResources().getColor(
					R.color.light_gray));
		} else {
			view.setBackgroundColor(Color.WHITE);
			statusTv.setVisibility(View.VISIBLE);
			statusTv.setText(getMessageStatus(flags));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		int flags = cursor.getInt(cursor.getColumnIndex(Messages.FLAGS));
		boolean isIncoming = Message.isIncoming(flags);
		if(isIncoming){
			return LayoutInflater.from(context).inflate(
					R.layout.incoming_chat_item, null);
		}
		return LayoutInflater.from(context).inflate(
				R.layout.outgoing_chat_item, null);
	}
	
	@Override
	public int getViewTypeCount() {	
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		Cursor cursor = (Cursor) getItem(position);
		int flags = cursor.getInt(cursor.getColumnIndex(Messages.FLAGS));
		boolean isIncoming = Message.isIncoming(flags);
		if(isIncoming){
			return 0;
		}
		return 1;
	}

	private String getMessageStatus(int flags) {
		int status = Message.getMessageStatus(flags);

		switch (status) {
		case Message.STATUS_SENDING:
			return "seding";

		case Message.STATUS_SENT:
			return "sent";
		case Message.STATUS_FAILED:
			return "failed";
		case Message.STATUS_DELIVERED:
			return "delivered";
		case Message.STATUS_READ:
			return "read";
		default:
			return "sending";
		}
	}
	
	class onThumbnailClickListener implements OnClickListener{
		
		private String messageId;
		private String type;
		private int position;
		
		public onThumbnailClickListener(String id, String type, int position){
			this.messageId = id;
			this.type = type;
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			if(Message.isPhotoAttachment(type)){
				Intent intent = new Intent(mContext, PhotoViewActivity.class);
				intent.putExtra(Messages._ID, messageId);
				mContext.startActivity(intent);
			}else if(Message.isVideoAttachment(type)){
				Intent intent = new Intent(mContext, VideoViewActivity.class);
				intent.putExtra(Messages._ID, messageId);
				mContext.startActivity(intent);
			}else if(Message.isGeoLocation(type)){
				int oldPossition = cursor.getPosition();
				cursor.moveToPosition(position);
				Message message = Message.createMessage(cursor);
				cursor.moveToPosition(oldPossition);
				Uri uri = message.getMapUri();
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
				mContext.startActivity(intent);
			}
		}
		
	}
}
