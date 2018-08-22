package com.parabit.parabeacon.app.demo.diagnotics;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.layout.BaseDemoFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DiagnoseFragment extends BaseDemoFragment {

    @BindView(R.id.fragment_diagnostics_msg_button) Button msgButton;
    @BindView(R.id.fragment_diagnostics_beacon_button) Button beaconButton;
    @BindView(R.id.fragment_diagnostics_clear_log_button) TextView clearLogButton;

    public static final String KEY_MSG_TYPE = "key_msg_type";
    public static final String NORMAL_MSG = "normal_msg";
    public static final String BEACON_INFO = "beacon_info";

    public static DiagnoseFragment newInstance() {
        DiagnoseFragment diagnoseFragment = new DiagnoseFragment();
        return diagnoseFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnostics, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        msgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DiagnoseDetailActivity.class);
                intent.putExtra(KEY_MSG_TYPE, NORMAL_MSG);
                startActivity(intent);
            }
        });

        beaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent beaconListIntent = new Intent(getActivity(), DiagnoseDetailActivity.class);
                beaconListIntent.putExtra(KEY_MSG_TYPE, BEACON_INFO);
                startActivity(beaconListIntent);
            }
        });

        clearLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: clear the log
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
