package com.parabit.parabeacon.app.tech.state;

import com.google.gson.annotations.Expose;
import com.parabit.beacon.api.BeaconInfo;

import java.util.Observable;

/**
 * Created by williamsnyder on 8/29/17.
 */

public class AppState extends Observable{

    @Expose
    private String username;

    @Expose
    private boolean persistentLogin = false;

    @Expose long sessionBegin;

    @Expose boolean debug = false;

    private BeaconInfo selectedBeacon;
    private long sessionTimeout = 60*60*1000;


    public String getUsername() {
        return username;
    }

    /*
    Used to silence change notifications when changing multiple properties
     */
    private boolean isAdjusting = false;

    public long getSessionBegin() {
        return sessionBegin;
    }

    public void setSessionBegin(long sessionBegin) {
        this.sessionBegin = sessionBegin;
    }

    public void startSession() {
        setSessionBegin(System.currentTimeMillis());
    }

    public void endSession() {
        setSessionBegin(0);
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * The username of the current user
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
        broadcastChange();
    }

    /**
     * @return true if the user should remain logged in between sessions
     */
    public boolean isPersistentLogin() {
        return persistentLogin;
    }

    /**
     * Should the user remain logged in between app sessions
     * @param persistentLogin true if the user should remain logged in
     */
    public void setPersistentLogin(boolean persistentLogin) {
        this.persistentLogin = persistentLogin;
        broadcastChange();
    }

    /**
     * Toggle app debugging
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return true if app debugging is enabled
     */
    public boolean isDebugEnabled() {
        return this.debug;
    }

    public void setSelectedBeacon(BeaconInfo beacon) {
        this.selectedBeacon = beacon;
    }

    public BeaconInfo getSelectedBeacon() {
        return selectedBeacon;
    }

    /**
     * Update the current AppState using the newState
     * @param newState values overwrite the current AppState
     */
    public void apply(AppState newState) {
        if (newState == null) {
            isAdjusting = true;
            setPersistentLogin(false);
            setUsername(null);
            setDebug(false);
            isAdjusting = false;
            broadcastChange();
            return;
        }
        isAdjusting = true;
        setUsername(newState.getUsername());
        setPersistentLogin(newState.isPersistentLogin());
        setDebug(newState.isDebugEnabled());
        isAdjusting = false;
        broadcastChange();
    }

    private void broadcastChange() {
        if (isAdjusting) {
            return;
        }
        setChanged();
        notifyObservers();
    }

}
