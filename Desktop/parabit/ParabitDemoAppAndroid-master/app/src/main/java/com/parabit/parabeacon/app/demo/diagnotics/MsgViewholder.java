package com.parabit.parabeacon.app.demo.diagnotics;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MsgViewholder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_msg_title) TextView msgTitle;
    @BindView(R.id.item_msg_type) TextView msgType;
    @BindView(R.id.item_msg_send) TextView msgSend;
    @BindView(R.id.item_msg_receive) TextView msgReceive;
    @BindView(R.id.item_successful_msg) TextView successMsg;
    @BindView(R.id.item_error_msg) TextView errorMsg;
    @BindView(R.id.item_door_belong_to) TextView doorBelong;
    @BindView(R.id.item_round_trip) TextView roundTrip;
    @BindView(R.id.item_msg_time) TextView msgTime;

    public MsgViewholder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
