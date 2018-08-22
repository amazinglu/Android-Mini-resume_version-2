package com.parabit.parabeacon.app.demo.diagnotics;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.layout.BaseDemoActivity;

public class DiagnoseDetailActivity extends BaseDemoActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        String msgType = getIntent().getStringExtra(DiagnoseFragment.KEY_MSG_TYPE);
        if (msgType.equals(DiagnoseFragment.NORMAL_MSG)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, MsgListFragment.newInstance())
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, BeaconListFragment.newInstance())
                    .commit();
        }
    }
}
