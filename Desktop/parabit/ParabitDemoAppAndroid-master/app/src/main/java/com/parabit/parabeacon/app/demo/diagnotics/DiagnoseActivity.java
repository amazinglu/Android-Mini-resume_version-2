package com.parabit.parabeacon.app.demo.diagnotics;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.layout.BaseDemoActivity;

import butterknife.BindView;

public class DiagnoseActivity extends BaseDemoActivity {

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, DiagnoseFragment.newInstance())
                .commit();
    }
}
