package com.parabit.parabeacon.app.demo.auth.layout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.parabit.parabeacon.app.demo.auth.AuthManager;
import com.parabit.parabeacon.app.demo.auth.state.AuthAppState;
import com.parabit.parabeacon.app.demo.auth.state.AuthAppStateManager;

/**
 * Created by williamsnyder on 11/6/17.
 */

public class BaseActivity extends AppCompatActivity {

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

    public AuthManager getAuthManager() {
        return AuthManager.getInstance(this);
    }

    public void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    public void showMessage(String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", listener).show();
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

    protected void launchMainActivity(){
        String mainClassname = getMetaData("MMRBT_POST_LOGIN_ACTIVITY");
        Class clazz = null;

        try {
            clazz = Class.forName(mainClassname);
        } catch (ClassNotFoundException e) {
        }

        if (mainClassname != null && clazz != null) {
            Intent intent = new Intent(this, clazz);
            startActivity(intent);
        }
    }

    protected String getMetaData(String key) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
