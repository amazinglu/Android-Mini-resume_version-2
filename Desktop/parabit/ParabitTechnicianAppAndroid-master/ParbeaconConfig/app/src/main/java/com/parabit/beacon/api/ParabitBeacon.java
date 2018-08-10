package com.parabit.beacon.api;

/**
 * Created by williamsnyder on 9/4/17.
 */

public class ParabitBeacon {

    private String name;
    private String description;
    private String uuid;

    public ParabitBeacon() {

    }

    public ParabitBeacon(String uuid, String name, String description) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
    }

    public ParabitBeacon(BeaconInfo beaconInfo) {
        this.uuid = beaconInfo.getUuid();
        this.name = beaconInfo.getName();
        this.description = beaconInfo.getLocation();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
