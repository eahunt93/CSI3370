package com.example.elija.sms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by elijah on 9/16/2017.
 */

public class ContactsAdapter extends ArrayAdapter<ContactObject> {
    /**
     * Create a new {@link ContactsAdapter} object.
     *
     * @param context is the current context (i.e. Activity) that the adapter is being created in.
     * @param contactObjects is the list of {@link ContactObject}s to be displayed.
     */
    public ContactsAdapter(Context context, ArrayList<ContactObject> contactObjects) {
        super(context, 0, contactObjects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Get the {@link Word} object located at this position in the list
       ContactObject currentContactObject = getItem(position);

        TextView name = (TextView)listItemView.findViewById(R.id.name);
        name.setText(currentContactObject.getContact());

        // Return the whole list item layout (containing 2 TextViews) so that it can be shown in
        // the ListView.
        return listItemView;
    }
}
