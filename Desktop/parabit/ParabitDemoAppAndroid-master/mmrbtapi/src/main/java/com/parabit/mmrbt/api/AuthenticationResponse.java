package com.parabit.mmrbt.api;

/**
 * Created by williamsnyder on 4/20/18.
 */

public class AuthenticationResponse {

    private String beaconURL;
    private String beaconKey;

    private String controlURL;
    private String controlKey;

    private String locationURL;
    private String locationKey;

    public String getBeaconURL() {
        return beaconURL;
    }

    public void setBeaconURL(String beaconURL) {
        this.beaconURL = beaconURL;
    }

    public String getBeaconKey() {
        return beaconKey;
    }

    public void setBeaconKey(String beaconKey) {
        this.beaconKey = beaconKey;
    }

    public String getControlURL() {
        return controlURL;
    }

    public void setControlURL(String controlURL) {
        this.controlURL = controlURL;
    }

    public String getControlKey() {
        return controlKey;
    }

    public void setControlKey(String controlKey) {
        this.controlKey = controlKey;
    }

    public String getLocationURL() {
        return locationURL;
    }

    public void setLocationURL(String locationURL) {
        this.locationURL = locationURL;
    }

    public String getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }
}
