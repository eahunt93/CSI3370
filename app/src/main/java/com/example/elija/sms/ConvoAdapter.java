package com.example.elija.sms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by elija on 9/22/2017.
 */

public class ConvoAdapter extends ArrayAdapter<ConversationObject> {

    private String formatDate(long dateObject){
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    private String formatTime(Date dateObject){
        SimpleDateFormat timeformat = new SimpleDateFormat("h:mm a");
        return timeformat.format(dateObject);
    }


    public ConvoAdapter(Context context, ArrayList<ConversationObject> convo) {
        super(context, 0, convo);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.message_item, parent, false);
        }

        ConversationObject currentConversationObject = getItem(position);
        TextView sent = (TextView)listItemView.findViewById(R.id.sent);
        sent.setText(currentConversationObject.getSentmessage());


        TextView recieved = (TextView)listItemView.findViewById(R.id.recieved);
        recieved.setText(currentConversationObject.getBody());
        return listItemView;
    }
}