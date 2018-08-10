package com.parabit.beacon.firmware;

/**
 * Created by williamsnyder on 10/12/17.
 */

public class FirmwareSummary {
    private FirmwareInfo current;
    private FirmwareInfo latest;
    private String latestURL;

    public FirmwareInfo getCurrent() {
        return current;
    }

    public void setCurrent(FirmwareInfo current) {
        this.current = current;
    }

    public FirmwareInfo getLatest() {
        return latest;
    }

    public void setLatest(FirmwareInfo latest) {
        this.latest = latest;
    }

    public String getLatestURL() {
        return latestURL;
    }

    public void setLatestURL(String latestURL) {
        this.latestURL = latestURL;
    }
}
