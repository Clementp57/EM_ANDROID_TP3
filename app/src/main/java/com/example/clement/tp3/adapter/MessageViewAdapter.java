package com.example.clement.tp3.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.clement.tp3.R;
import com.example.clement.tp3.model.Group;
import com.example.clement.tp3.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Clement on 16/07/15.
 */
public class MessageViewAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Message> messages = new ArrayList<Message>();

    public MessageViewAdapter(Activity activity, List<Message> messages) {
        this.activity = activity;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_message, null);
        }

        TextView author =  (TextView) convertView.findViewById(R.id.author);
        TextView messageContent =  (TextView) convertView.findViewById(R.id.messageContent);

        Message message = messages.get(position);
        author.setText(message.getAuthorId());

        messageContent.setText(message.getContent());
        return convertView;
    }
}
