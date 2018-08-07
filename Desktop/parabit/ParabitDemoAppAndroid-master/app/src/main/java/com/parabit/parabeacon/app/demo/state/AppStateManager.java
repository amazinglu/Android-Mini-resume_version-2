package com.parabit.parabeacon.app.demo.state;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parabit.parabeacon.app.demo.BuildConfig;

/**
 * Created by williamsnyder on 8/29/17.
 */

/**
 * Manager of the AooState object
 * */
public class AppStateManager {
    private static final String TAG = AppStateManager.class.getSimpleName();
    private static final String SAVED_STATE = "DEMO_SAVED_STATE_"+BuildConfig.BUILD_TYPE;
    private static final String APP_STATE = "DEMO_APP_STATE_"+BuildConfig.BUILD_TYPE;

    private final Context activity;
    private final Gson gson;

    private static AppState _currentState;

    public AppStateManager(Context context) {
        this.activity = context;
        // Configures Gson to exclude all fields from consideration
        // for serialization or deserialization that do not have the Expose annotation.
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

    /**
     * load the appState from sharePreference
     * */
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
