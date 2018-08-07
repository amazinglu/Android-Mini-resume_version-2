package com.parabit.parabeacon.app.demo.layout;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.R;

import net.hockeyapp.android.metrics.MetricsManager;

/**
 * Created by williamsnyder on 8/28/17.
 */

public class AboutFragment extends BaseDemoFragment {

    public static AboutFragment newInstance(Bundle bundle) {
        AboutFragment aboutFragment = new AboutFragment();
        aboutFragment.setArguments(bundle);
        return aboutFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_about, container, false);

        setupView(fragmentView);

        return fragmentView;
    }

    private void setupView(View fragmentView) {
        TextView mTextViewUsername = (TextView) fragmentView.findViewById(R.id.txtViewUsername);

        if (getCurrentState() != null) {
            mTextViewUsername.setText(getCurrentState().getUsername());
        }

        Button mButtonLogout = (Button) fragmentView.findViewById(R.id.btnLogout);
        mButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogout();
            }
        });

        TextView mTextVersion = (TextView) fragmentView.findViewById(R.id.txtViewVersion);
        PackageManager manager = fragmentView.getContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(
                    fragmentView.getContext().getPackageName(), 0);
            String version = info.versionName;
            mTextVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            //ignore
        }

    }

    private void handleLogout() {
        MetricsManager.trackEvent("APP_LOGOUT");
        getCurrentState().setPersistentLogin(false);
        saveAppState();
        Intent intent = new Intent(AboutFragment.this.getContext(), LoginActivity.class);
        startActivity(intent);
    }

}