package com.tdt.js_v2;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by TE-iA on 03/29/17.
 */

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    private Context context;
    private int resId;
    private List<BluetoothDevice> data;

    public DeviceListAdapter(Context context, int resource, List<BluetoothDevice> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resId = resource;
        this.data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resId, parent, false);
        Log.d("aa", "bbbb.");
        BluetoothDevice device = data.get(position);
        if (device != null) {
            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            TextView tvAddress = (TextView) view.findViewById(R.id.tvAddress);
            if (tvName != null) {
                tvName.setText(device.getName() + ": ");
            }
            if (tvAddress != null) {
                tvAddress.setText(device.getAddress() + "");
            }
        }
        return view;
    }
}
