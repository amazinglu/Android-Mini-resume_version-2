package com.parabit.parabeacon.app.demo.diagnotics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.state.Door;
import com.parabit.parabeacon.app.demo.util.DateUtil;

import java.util.List;

public class BeaconListAdapter extends RecyclerView.Adapter {

    private List<Door> beaconList;
    private Context context;

    public BeaconListAdapter(List<Door> beaconList) {
        this.beaconList = beaconList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diagonstics_beacon,
                parent, false);
        return new BeaconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Door beacon = beaconList.get(position);
        BeaconViewHolder viewHolder = (BeaconViewHolder) holder;
        setText(viewHolder, beacon);
    }

    @Override
    public int getItemCount() {
        return beaconList.size();
    }

    private void setText(BeaconViewHolder viewHolder, Door beacon) {
        viewHolder.beaconIdView.setText(getContext().getResources().getString(R.string.beacon_id)
                + " " + beacon.getUuid());
        viewHolder.doorNameView.setText(getContext().getResources().getString(R.string.door_name)
                + " " + beacon.getName());
        viewHolder.serialNumberView.setText(getContext().getResources().getString(R.string.beacon_serial_number)
                + " " + beacon.getSerialNumber());
        viewHolder.locationView.setText(getContext().getResources().getString(R.string.beacon_location)
                + " " + beacon.getLocation());
        // TODO: need to add the date into Door object
        viewHolder.beaconDateView.setText(getContext().getResources().getString(R.string.beacon_date)
                + " ");
    }

    private Context getContext() {
        return context;
    }
}
