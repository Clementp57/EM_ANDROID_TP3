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

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Clement on 15/07/15.
 */
public class GroupViewAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Group> groups;

    public GroupViewAdapter(Activity activity, List<Group> groups) {
        this.activity = activity;
        this.groups = groups;
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
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
            convertView = inflater.inflate(R.layout.list_row, null);
        }

        TextView groupName =  (TextView) convertView.findViewById(R.id.groupName);
        TextView groupUsers =  (TextView) convertView.findViewById(R.id.groupUsers);

        Group group = groups.get(position);
        groupName.setText(group.getName());

        String users = "";
        for(String user : group.getUsers()) {
            users += "<"+user + ">, ";
        }
        users = users.length() > 0 ? users.substring(0,
                users.length() - 2) : users;



        groupUsers.setText(users);

        return convertView;

    }
}
