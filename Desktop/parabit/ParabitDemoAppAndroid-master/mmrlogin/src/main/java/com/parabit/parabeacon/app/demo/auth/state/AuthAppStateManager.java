package com.parabit.parabeacon.app.demo.auth.state;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by williamsnyder on 8/29/17.
 */

public class AuthAppStateManager {
    private static final String TAG = AuthAppStateManager.class.getSimpleName();
    private static final String SAVED_STATE = "TECH_APP_SAVED_STATE";
    private static final String APP_STATE = "TECH_APP_STATE";

    private final Context activity;
    private final Gson gson;

    private static AuthAppState _currentState;

    private static AuthAppStateManager instance;

    private AuthAppStateManager(Context context) {
        this.activity = context.getApplicationContext();
        this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public static AuthAppStateManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthAppStateManager(context.getApplicationContext());
        }
        return instance;
    }

    public AuthAppState currentState() {
        if (_currentState == null) {
            load();
        }

        return _currentState;
    }

    public void update(AuthAppState newState) {
        if (_currentState == null) {
            _currentState = new AuthAppState();
        }
        _currentState.apply(newState);
        SharedPreferences.Editor spEditor = activity.getSharedPreferences(
                SAVED_STATE, Context.MODE_PRIVATE).edit();

        String serializedConfiguration = gson.toJson(_currentState);
        spEditor.putString(APP_STATE, serializedConfiguration);
        spEditor.apply();
    }

    public void load() {
        SharedPreferences sp = activity.getSharedPreferences(
                SAVED_STATE, Context.MODE_PRIVATE);
        String serializedConfiguration = sp.getString(APP_STATE, "");
        AuthAppState stateFromDisk
                = gson.fromJson(serializedConfiguration, AuthAppState.class);

        if (_currentState == null) {
            _currentState = new AuthAppState();
        }
        _currentState.apply(stateFromDisk);
    }

}
