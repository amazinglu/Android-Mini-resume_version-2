package com.parabit.parabeacon.app.demo.state;

import com.google.gson.annotations.Expose;
import com.parabit.mmrbt.api.BankLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by williamsnyder on 8/29/17.
 */

public class AppState extends Observable{

    @Expose
    private String username;

    @Expose
    private boolean persistentLogin = false;

    @Expose
    private int doorOpenTime = 5;

    @Expose
    private boolean demoMode = false;

    private List<Door> availableDoors = new ArrayList<>();

    private BankLocation selectedLocation;

    @Expose
    private boolean ignoreDoor;

    private List<BankLocation.EmergencyContact> contacts;

    public String getUsername() {
        return username;
    }

    /*
    Used to silence change notifications when changing multiple properties
     */
    private boolean isAdjusting = false;

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
     * Get the Door Open time (in seconds)
     * @return the door open time (in seconds)
     */
    public int getDoorOpenTime() {
        return doorOpenTime;
    }

    /**
     * Set the Door Open time (in seconds)
     * @param doorOpenTime seconds the door should remain open
     */
    public void setDoorOpenTime(int doorOpenTime) {
        this.doorOpenTime = doorOpenTime;
        broadcastChange();
    }


    public List<Door> getAvailableDoors() {
        return availableDoors;
    }

    public void setAvailableDoors(List<Door> availableDoors) {
        this.availableDoors = availableDoors;
        broadcastChange();
    }

    public void addAvailableDoor(Door door) {
        if (availableDoors.contains(door)) {
            return;
        }
        this.availableDoors.add(door);
        broadcastChange();
    }

    public void removeAvailableDoor(String serialNumber) {
        if (serialNumber == null) {
            return;
        }

        List filteredDoors = new ArrayList();
        for (Door door: availableDoors) {
            if (!serialNumber.equals(door.getSerialNumber())) {
                filteredDoors.add(door);
            }
        }

        availableDoors.clear();
        availableDoors.addAll(filteredDoors);

        broadcastChange();
    }

    public void clearAvailableDoors() {
        this.availableDoors.clear();
        broadcastChange();
    }

    public BankLocation getSelectedLocation() {
        return selectedLocation;
    }

    public void setSelectedLocation(BankLocation selectedLocation) {
        this.selectedLocation = selectedLocation;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }

    public List<BankLocation.EmergencyContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<BankLocation.EmergencyContact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Update the current AppState using the newState
     * @param newState values overwrite the current AppState
     */
    public void apply(AppState newState) {
        if (newState == null) {
            isAdjusting = true;
            setDoorOpenTime(5);
            setPersistentLogin(false);
            setUsername(null);
            clearAvailableDoors();
            setIgnoreDoor(false);
            setSelectedLocation(null);
            isAdjusting = false;
            broadcastChange();
            setDemoMode(false);
            setContacts(null);
            return;
        }
        isAdjusting = true;
        setDoorOpenTime(newState.getDoorOpenTime());
        setUsername(newState.getUsername());
        setPersistentLogin(newState.isPersistentLogin());
        setAvailableDoors(newState.getAvailableDoors());
        setIgnoreDoor(newState.ignoreDoor());
        setDemoMode(newState.isDemoMode());
        setSelectedLocation(newState.getSelectedLocation());
        setContacts(newState.getContacts());
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

    public void setIgnoreDoor(boolean ignoreDoor) {
        this.ignoreDoor = ignoreDoor;
    }

    public boolean ignoreDoor() {
        return this.ignoreDoor;
    }

}
