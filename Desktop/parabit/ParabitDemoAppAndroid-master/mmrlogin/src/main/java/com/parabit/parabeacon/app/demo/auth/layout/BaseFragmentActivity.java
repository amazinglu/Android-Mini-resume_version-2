package com.parabit.parabeacon.app.demo.auth.layout;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.parabit.parabeacon.app.demo.auth.state.AuthAppState;
import com.parabit.parabeacon.app.demo.auth.state.AuthAppStateManager;

/**
 * Created by williamsnyder on 12/8/17.
 */

public class BaseFragmentActivity extends FragmentActivity {

    private AuthAppState currentState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveAppState();
    }

    public AuthAppStateManager getAppStateManager() {
        return AuthAppStateManager.getInstance(this);
    }

    public AuthAppState getCurrentState() {
        if (currentState == null) {
            currentState = getAppStateManager().currentState();
        }
        return currentState;
    }

    public void loadAppState() {
        getAppStateManager().load();
    }

    public void saveAppState() {
        getAppStateManager().update(getCurrentState());
    }
}
