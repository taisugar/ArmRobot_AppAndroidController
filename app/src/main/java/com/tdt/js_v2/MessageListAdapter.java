package com.tdt.js_v2;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by TE-iA on 04/05/17.
 */

public class MessageListAdapter extends ArrayAdapter<String> {
    private Context context;
    private int resId;
    private List<String> data;

    public MessageListAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resId = resource;
        this.data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resId, parent, false);

        String message = data.get(position);
        if (message != null) {
            TextView tvMessages = (TextView) view.findViewById(R.id.tvMessages);

            if (tvMessages != null) {
                tvMessages.setText(message);
            }
        }
        return view;
    }
}
