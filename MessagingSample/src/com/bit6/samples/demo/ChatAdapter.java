package com.bit6.samples.demo;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bit6.sdk.Message;
import com.bit6.sdk.Message.Messages;

public class ChatAdapter extends CursorAdapter {

	public ChatAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String content = cursor.getString(cursor
				.getColumnIndex(Messages.CONTENT));
		TextView contentTv = (TextView) view.findViewById(R.id.content);
		contentTv.setText(content);

		TextView statusTv = (TextView) view.findViewById(R.id.status);
		TextView dateTv = (TextView) view.findViewById(R.id.date);

		long stamp = cursor.getLong(cursor.getColumnIndex(Messages.UPDATED));
		if (stamp == 0) {
			stamp = cursor.getLong(cursor.getColumnIndex(Messages.CREATED));
		}
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT);
		String stampString = df.format(new Date(stamp));

		dateTv.setText(stampString);

		int flags = cursor.getInt(cursor.getColumnIndex(Messages.FLAGS));

		if (Message.isIncoming(flags)) {
			contentTv.setGravity(Gravity.LEFT);
			statusTv.setVisibility(View.GONE);
			view.setBackgroundColor(context.getResources().getColor(
					R.color.light_gray));
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			dateTv.setLayoutParams(params);
		} else {
			view.setBackgroundColor(Color.WHITE);
			contentTv.setGravity(Gravity.RIGHT);
			statusTv.setVisibility(View.VISIBLE);
			statusTv.setText(getMessageStatus(flags));
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
			dateTv.setLayoutParams(params);
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		return LayoutInflater.from(context).inflate(
				R.layout.conversation_list_item, null);
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
}
