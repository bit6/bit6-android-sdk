
package com.bit6.samples.demo;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bit6.samples.demo.imagecache.ImageFetcher;
import com.bit6.sdk.Message;
import com.bit6.sdk.db.Contract;

public class ChatAdapter extends CursorAdapter {

    private Context mContext;
    private ImageFetcher mImageFetcher;

    public ChatAdapter(Context context, Cursor c, ImageFetcher imageFetcher, boolean autoRequery) {
        super(context, c, autoRequery);
        this.mContext = context;
        this.mImageFetcher = imageFetcher;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        String content = cursor.getString(cursor.getColumnIndex(Contract.Messages.CONTENT));
        TextView contentTv = (TextView) view.findViewById(R.id.content);
        contentTv.setText(content);

        TextView statusTv = (TextView) view.findViewById(R.id.status);
        TextView dateTv = (TextView) view.findViewById(R.id.date);

        ImageView thumb = (ImageView) view.findViewById(R.id.thumb);
        ImageView video = (ImageView) view.findViewById(R.id.video_icon);

        thumb.setVisibility(View.GONE);
        video.setVisibility(View.GONE);

        long stamp = cursor.getLong(cursor.getColumnIndex(Contract.Messages.UPDATED));
        if (stamp == 0) {
            stamp = cursor.getLong(cursor.getColumnIndex(Contract.Messages.CREATED));
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String stampString = df.format(new Date(stamp));

        dateTv.setText(stampString);

        int flags = cursor.getInt(cursor.getColumnIndex(Contract.Messages.FLAGS));

        int type = Contract.Messages.getType(flags);
        
        if ( type == Contract.Messages.TYPE_ATTACH || type == Contract.Messages.TYPE_GEOLOC) {
            thumb.setVisibility(View.VISIBLE);

            // Thumbnail image for this message, if any
            String thumbUri = cursor.getString(cursor.getColumnIndex(Contract.Messages.THUMB_URI));
            if (thumbUri != null) {
                mImageFetcher.loadImage(thumbUri, thumb);
            }

            // Additional data for the message - attachment, geo location etc
            String dataStr = cursor.getString(cursor.getColumnIndex(Contract.Messages.DATA));
            Message.Data data = Message.getData(dataStr, flags);
            
            // Intent that will be used when this item is clicked
            Intent intent = null;

            // Message has an attachment
            if (data instanceof Message.Attachment) {
                Message.Attachment attach = (Message.Attachment)data;
                if (attach != null) {
                    String ctype = attach.getContentType();
                    // Show media playing icon
                    if (ctype != null && ctype.startsWith("video")) {
                        video.setVisibility(View.VISIBLE);
                    }
                    intent = new Intent(Intent.ACTION_VIEW); 
                    intent.setDataAndType(Uri.parse(attach.getUri()), ctype);
                }
            }
            // Message has a geo location
            else if (data instanceof Message.GeoLocation) {
                Message.GeoLocation geo = (Message.GeoLocation)data;
                Uri uri = geo.getMapUri();
                intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
            }

            // Add a listener for a click on the thumbnail to view the attachment
            // or the map location
            if (intent != null) {
                thumb.setOnClickListener(new OnThumbnailClickListener(intent));
            }
        }

        // Show status
        if (Contract.Messages.isIncoming(flags)) {
            statusTv.setVisibility(View.GONE);
        } else {
            statusTv.setVisibility(View.VISIBLE);
            statusTv.setText(getMessageStatus(flags));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        int flags = cursor.getInt(cursor.getColumnIndex(Contract.Messages.FLAGS));
        boolean isIncoming = Contract.Messages.isIncoming(flags);
        int resId = isIncoming ? R.layout.incoming_chat_item : R.layout.outgoing_chat_item;
        return LayoutInflater.from(context).inflate(resId, null);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        int flags = cursor.getInt(cursor.getColumnIndex(Contract.Messages.FLAGS));
        boolean isIncoming = Contract.Messages.isIncoming(flags);
        return isIncoming ? 0 : 1;
    }

    private String getMessageStatus(int flags) {
        int status = Contract.Messages.getStatus(flags);

        switch (status) {
            case Contract.Messages.STATUS_SENDING:
                return "sending";
            case Contract.Messages.STATUS_SENT:
                return "sent";
            case Contract.Messages.STATUS_FAILED:
                return "failed";
            case Contract.Messages.STATUS_DELIVERED:
                return "delivered";
            case Contract.Messages.STATUS_READ:
                return "read";
            default:
                return "sending";
        }
    }

    class OnThumbnailClickListener implements OnClickListener {

        private Intent target;

        public OnThumbnailClickListener(Intent target) {
            this.target = target;
        }

        @Override
        public void onClick(View v) {
            mContext.startActivity(target);
        }

    }
}
