package com.parabit.parabeacon.app.demo.diagnotics;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BeaconViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_beacon_id) TextView beaconIdView;
    @BindView(R.id.item_door_name) TextView doorNameView;
    @BindView(R.id.item_beacon_serial_number) TextView serialNumberView;
    @BindView(R.id.item_beacon_location) TextView locationView;
    @BindView(R.id.item_beacon_date) TextView beaconDateView;

    public BeaconViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
