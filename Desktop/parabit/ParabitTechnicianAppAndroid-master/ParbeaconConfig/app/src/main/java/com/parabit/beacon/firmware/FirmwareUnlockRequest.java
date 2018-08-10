package com.parabit.beacon.firmware;

import com.google.gson.annotations.SerializedName;

/**
 * Created by williamsnyder on 11/13/17.
 */

public class FirmwareUnlockRequest {

    private String challenge;

    @SerializedName("firmware_revision")
    private String firmwareRevision;

    public String getFirmwareRevision() {
        return firmwareRevision;
    }

    public void setFirmwareRevision(String firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }
}
