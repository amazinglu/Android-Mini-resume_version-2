package com.parabit.parabeacon.app.demo.diagnotics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.model.DiagnosticMsg;

import java.util.List;

public class MsgListAdapter extends RecyclerView.Adapter {

    private List<DiagnosticMsg> msgList;
    private Context context;

    public MsgListAdapter(List<DiagnosticMsg> msgList) {
        this.msgList = msgList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diagnostics_msg,
                parent, false);
        this.context = parent.getContext();
        return new MsgViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DiagnosticMsg msg = msgList.get(position);
        MsgViewholder viewholder = (MsgViewholder) holder;
        setText(viewholder, msg);
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    private void setText(MsgViewholder viewholder, DiagnosticMsg msg) {
        viewholder.msgTitle.setText(getContext().getResources().getString(R.string.smg_title) + " "
                + msg.getMsgTitle());
        viewholder.msgType.setText(getContext().getResources().getString(R.string.smg_type) + " "
                + msg.getMsgType());
        viewholder.msgSend.setText(getContext().getResources().getString(R.string.is_msg_send) +
                " " + (msg.isMsgSend() ? "true" : "false"));
        viewholder.msgReceive.setText(getContext().getResources().getString(R.string.is_msg_receive) +
                " " + (msg.isMsgReceive() ? "true" : "false"));
        viewholder.successMsg.setText(getContext().getResources().getString(R.string.msg_success) + " "
                + msg.getSuccessMsg());
        viewholder.errorMsg.setText(getContext().getResources().getString(R.string.msg_error) + " "
                + msg.getErrorMsg());
        viewholder.doorBelong.setText(getContext().getResources().getString(R.string.msg_door_belong) +
                " " + msg.getDoor().getName());
        // TODO: date to string and string to date
        viewholder.msgTime.setText(getContext().getResources().getString(R.string.msg_date) + " "
                + msg.getMsgDate().toString());
        viewholder.roundTrip.setText(getContext().getResources().getString(R.string.msg_round_trip)
                + " " + Double.toString(msg.getRoundTrip()));
    }

    public Context getContext() {
        return context;
    }
}
