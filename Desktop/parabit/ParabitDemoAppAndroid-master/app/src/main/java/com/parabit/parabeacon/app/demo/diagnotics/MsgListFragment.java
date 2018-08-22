package com.parabit.parabeacon.app.demo.diagnotics;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.layout.BaseDemoFragment;
import com.parabit.parabeacon.app.demo.model.DiagnosticMsg;
import com.parabit.parabeacon.app.demo.state.AppState;
import com.parabit.parabeacon.app.demo.state.AppStateManager;
import com.parabit.parabeacon.app.demo.state.Door;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MsgListFragment extends BaseDemoFragment {

    @BindView(R.id.diagnostics_msg_list_view) RecyclerView msgListView;

    private List<DiagnosticMsg> msgList;

    public static MsgListFragment newInstance() {
        return new MsgListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_msg_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        loadMsgList();

        msgListView.setLayoutManager(new LinearLayoutManager(getContext()));
        msgListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        MsgListAdapter adapter = new MsgListAdapter(msgList);
        msgListView.setAdapter(adapter);
    }

    private void loadMsgList() {
        if (msgList == null) {
            msgList = new ArrayList<>();
        } else {
            msgList.clear();
        }

        // TODO: load the msg from the file
        msgList = fakeData();
    }

    /**
     * fake data for test purpose
     * */
    private List<DiagnosticMsg> fakeData() {
        List<DiagnosticMsg> msgList = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            msgList.add(getFakeMsg());
        }
        return msgList;
    }

    private DiagnosticMsg getFakeMsg() {
        DiagnosticMsg msg = new DiagnosticMsg();
        msg.setMsgTitle("test msg");
        msg.setMsgType("test type");
        msg.setMsgId(UUID.randomUUID().toString());
        msg.setMsgSend(true);
        msg.setMsgReceive(true);
        msg.setSuccessMsg("getting response");
        msg.setErrorMsg("can not get response");
        msg.setRoundTrip(3.0);
        msg.setMsgDate(Calendar.getInstance().getTime());
        Door door = new Door();
        door.setName("Ben's door");
        door.setLocation("office");
        door.setSerialNumber("167772170");
        door.setUuid(UUID.randomUUID().toString());
        msg.setDoor(door);
        return msg;
    }
}
