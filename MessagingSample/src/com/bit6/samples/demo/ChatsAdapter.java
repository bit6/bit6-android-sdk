package com.bit6.samples.demo;

import com.bit6.sdk.Message.Messages;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ChatsAdapter extends CursorAdapter{

	public ChatsAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		String userName = cursor.getString(cursor.getColumnIndex(Messages.OTHER));
		String content = cursor.getString(cursor.getColumnIndex(Messages.CONTENT));
		
		//User names are saved like usr:userName, so deleting that part of user names
		if(userName.contains(":")){
			userName = userName.substring(userName.indexOf(':')+1);
		}
		
		TextView nameTv = (TextView) view.findViewById(R.id.userName);
		TextView contentTv = (TextView) view.findViewById(R.id.content);
		nameTv.setText(userName);
		contentTv.setText(content);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		return LayoutInflater.from(context).inflate(R.layout.chats_list_item, null);
	}
	
	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}

}
