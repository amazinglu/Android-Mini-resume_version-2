package com.parabit.mmrbt.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by williamsnyder on 2/13/18.
 */

public class DeviceRegistrationResult {

    @SerializedName("secret")
    private String token;

    @SerializedName("id")
    private String deviceId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
