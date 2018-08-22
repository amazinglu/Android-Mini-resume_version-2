package com.parabit.parabeacon.app.demo.diagnotics;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.layout.BaseDemoFragment;

import butterknife.BindView;

public class DiagnoseFragment extends BaseDemoFragment {

    @BindView(R.id.fragment_diagnostics_msg_button) Button msgButton;
    @BindView(R.id.fragment_diagnostics_beacon_button) Button beaconButton;
    @BindView(R.id.fragment_diagnostics_clear_log_button) Button clearLogButton;

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
        msgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: jump to msg list fragment
            }
        });

        beaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: jump to beacon lsit beacon
            }
        });

        clearLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: clear the log
            }
        });
    }
}
