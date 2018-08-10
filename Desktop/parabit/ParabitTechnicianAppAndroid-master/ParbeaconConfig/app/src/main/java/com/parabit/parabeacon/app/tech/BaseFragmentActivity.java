package com.parabit.parabeacon.app.tech;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.parabit.parabeacon.app.tech.auth.AuthManager;
import com.parabit.parabeacon.app.tech.state.AppState;
import com.parabit.parabeacon.app.tech.state.AppStateManager;
import com.parabit.parabeacon.app.tech.utils.UiUtils;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManager;

/**
 * Created by williamsnyder on 12/8/17.
 */

public class BaseFragmentActivity extends FragmentActivity {

    private AppState currentState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerForUpdates();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppState();
        Tracking.startUsage(this);
        CrashManager.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveAppState();
        Tracking.stopUsage(this);
        unregisterForUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterForUpdates();
    }

    private void registerForUpdates() {
        UpdateManager.register(this);
    }

    private void unregisterForUpdates() {
        UpdateManager.unregister();
    }

    public AuthManager getAuthManager() {
        return ((MainApplication)getApplication()).getAuthManager();
    }

    public void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    public void showMessage(String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", listener).show();
    }

    public void showPopupMessage(String message) {
        UiUtils.showToast(this, message);
    }


    public AppStateManager getAppStateManager() {
        if (getApplication() instanceof MainApplication) {
            return ((MainApplication) getApplication()).getAppStateManager();
        }
        return null;
    }

    public AppState getCurrentState() {
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
