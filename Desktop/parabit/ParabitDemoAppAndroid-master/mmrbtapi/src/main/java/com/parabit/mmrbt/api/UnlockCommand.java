package com.parabit.mmrbt.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by williamsnyder on 2/13/18.
 */

public class UnlockCommand {

    @SerializedName("device_id")
    private String deviceId;

    private String token;

    @SerializedName("serial_number")
    private String serialNumber;

    @SerializedName("door_open_duration")
    private int doorOpenTime;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getDoorOpenTime() {
        return doorOpenTime;
    }

    public void setDoorOpenTime(int doorOpenTime) {
        this.doorOpenTime = doorOpenTime;
    }
}
