package com.parabit.parabeacon.app.tech.state;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by williamsnyder on 8/29/17.
 */

public class AppStateManager {
    private static final String TAG = AppStateManager.class.getSimpleName();
    private static final String SAVED_STATE = "TECH_APP_SAVED_STATE";
    private static final String APP_STATE = "TECH_APP_STATE";

    private final Context activity;
    private final Gson gson;

    private static AppState _currentState;

    public AppStateManager(Context context) {
        this.activity = context;
        this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public AppState currentState() {
        if (_currentState == null) {
            load();
        }

        return _currentState;
    }

    public void update(AppState newState) {
        if (_currentState == null) {
            _currentState = new AppState();
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
        AppState stateFromDisk
                = gson.fromJson(serializedConfiguration, AppState.class);

        if (_currentState == null) {
            _currentState = new AppState();
        }
        _currentState.apply(stateFromDisk);
    }

}
