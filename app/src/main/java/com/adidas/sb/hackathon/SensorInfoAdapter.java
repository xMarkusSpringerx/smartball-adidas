package com.adidas.sb.hackathon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by domenm on 17.8.2016.
 */
public class SensorInfoAdapter extends ArrayAdapter<SensorInfo> {
    public SensorInfoAdapter(Context context, ArrayList<SensorInfo> sensors) {
        super(context, 0, sensors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SensorInfo sensorInfo = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_sensor,  parent, false);
        }

        TextView tvName = (TextView) convertView.findViewById(R.id.tv_sensor_name);
        TextView tvSignal = (TextView) convertView.findViewById(R.id.tv_signal_strength);

        tvName.setText(sensorInfo.getName());
        tvSignal.setText(String.valueOf(sensorInfo.getRssi()));

        return convertView;
    }
}
