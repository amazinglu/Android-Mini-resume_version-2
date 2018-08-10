package com.parabit.beacon.firmware;

import com.google.gson.annotations.SerializedName;

/**
 * Created by williamsnyder on 10/12/17.
 */

public class FirmwareInfo {

    private String id;
    private String revision;

    @SerializedName("unlock_code")
    private String unlockCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getUnlockCode() {
        return unlockCode;
    }

    public void setUnlockCode(String unlockCode) {
        this.unlockCode = unlockCode;
    }
}
