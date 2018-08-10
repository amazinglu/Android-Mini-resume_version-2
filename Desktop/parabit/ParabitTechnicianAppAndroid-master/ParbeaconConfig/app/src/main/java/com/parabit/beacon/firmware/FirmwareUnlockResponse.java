package com.parabit.beacon.firmware;

import com.google.gson.annotations.SerializedName;

/**
 * Created by williamsnyder on 11/10/17.
 */

public class FirmwareUnlockResponse {
    @SerializedName("unlock_response")
    private String unlockResponse;

    public String getUnlockResponse() {
        return unlockResponse;
    }

}
