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
import com.parabit.parabeacon.app.demo.state.Door;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BeaconListFragment extends BaseDemoFragment {

    @BindView(R.id.diagnostics_beacon_list_view) RecyclerView beaconListView;

    private BeaconListAdapter adapter;
    private List<Door> beaconList;

    public static BeaconListFragment newInstance() {
        return new BeaconListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beacon_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        loadData();

        beaconListView.setLayoutManager(new LinearLayoutManager(getContext()));
        beaconListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        adapter = new BeaconListAdapter(beaconList);
        beaconListView.setAdapter(adapter);
    }

    private void loadData() {
        if (beaconList == null) {
            beaconList = new ArrayList<>();
        } else {
            beaconList.clear();
        }

        beaconList = fakeData();
    }

    private List<Door> fakeData() {
        List<Door> doorList = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            doorList.add(fakeDoor());
        }
        return doorList;
    }

    private Door fakeDoor() {
        Door door = new Door();
        door.setName("Ben's door");
        door.setLocation("office");
        door.setSerialNumber("167772170");
        door.setUuid(UUID.randomUUID().toString());
        return door;
    }
}
