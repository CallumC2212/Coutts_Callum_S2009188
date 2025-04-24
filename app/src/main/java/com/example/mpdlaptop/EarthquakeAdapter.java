package com.example.mpdlaptop;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class EarthquakeAdapter extends ArrayAdapter<MainActivity.earthQuakeData> {

    private final Context context;
    private final ArrayList<MainActivity.earthQuakeData> values;
    private boolean showDistance = false;


    public EarthquakeAdapter(Context context, ArrayList<MainActivity.earthQuakeData> values) {
        super(context, 0, values);
        this.context = context;
        this.values = values;
    }
    public void setShowDistance(boolean show) {
        this.showDistance = show;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.earthquake_item, parent, false);
        }

        MainActivity.earthQuakeData quake = values.get(position);

        TextView title = convertView.findViewById(R.id.quake_title);
        TextView location = convertView.findViewById(R.id.quake_location);
        TextView magnitude = convertView.findViewById(R.id.quake_magnitude);
        TextView depth = convertView.findViewById(R.id.quake_depth);
        TextView date = convertView.findViewById(R.id.quake_date);
        TextView distance = convertView.findViewById(R.id.quake_distance);
        TextView description = convertView.findViewById(R.id.quake_description);

        title.setText(quake.getTitle());
        location.setText("Location: " + quake.getLocation());
        magnitude.setText("Magnitude: " + quake.getMagnitude());
        depth.setText("Depth: " + quake.getDepth());
        date.setText("Date: " + quake.getDatetime());
        description.setText(quake.getDescription());

        description.setVisibility(quake.isExpanded() ? View.VISIBLE : View.GONE);

        if (showDistance) {
            distance.setVisibility(View.VISIBLE);
            distance.setText("Distance from Glasgow: " + Math.round(quake.getDistanceFromG()  * 100.0) / 100.0+ " km");
        } else {
            distance.setVisibility(View.GONE);
        }
        double mag =  Double.parseDouble(quake.getMagnitude());
        int color;

        if (mag < 1.0) {
            color = Color.parseColor("#A8E6CF"); // Light green
        } else if (mag < 3.0) {
            color = Color.parseColor("#FFD3B6"); // Orange
        } else if (mag < 6.0) {
            color = Color.parseColor("#FF8B94"); // Pink
        } else {
            color = Color.parseColor("#FF5252"); // Red
        }

        // Apply background color to magnitudeView or whole item
        magnitude.setBackgroundColor(color);

        return convertView;
    }
}