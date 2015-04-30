
package com.bit6.samples.demo;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bit6.sdk.db.Contract;

public class ChatsAdapter extends CursorAdapter {

    public ChatsAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Address
        String other = cursor.getString(cursor.getColumnIndex(Contract.Conversations.ID));
        // Last message text
        String content = cursor.getString(cursor.getColumnIndex(Contract.Messages.CONTENT));
        // Unread message count
        int count = cursor.getInt(cursor.getColumnIndex(Contract.Conversations.UNREAD_MESSAGE_COUNT));

        // This is an Address of the user we have a conversation with.
        // For simplicity, just show it without the URI scheme
        int pos = other.indexOf(':');
        if (pos >= 0) {
            other = other.substring(pos+1);
        }

        TextView nameTv = (TextView) view.findViewById(R.id.userName);
        TextView contentTv = (TextView) view.findViewById(R.id.content);
        TextView unreadTv = (TextView) view.findViewById(R.id.badge);
        nameTv.setText(other);
        contentTv.setText(content);
        if(count > 0){
            unreadTv.setText(String.valueOf(count));
        }else{
            unreadTv.setText("");
        }
        
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
