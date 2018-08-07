package com.parabit.mmrbt.api;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by williamsnyder on 9/8/17.
 */

public class BeaconInfo {
    private String name;
    private String location;

    @SerializedName("id")
    private String uuid;

    @SerializedName("location_id")
    private String bankLocationId;

    @SerializedName("mac_address")
    private String macAddress;

    @SerializedName("serial_number")
    private String serialNumber;

    @SerializedName("instance_id")
    private String instanceID;

    private String namespace;

    private BankLocation bankLocation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String toJSON() {
        return new Gson().toJson(this).toString();
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getBankLocationId() {
        return bankLocationId;
    }

    public void setBankLocationId(String bankLocationId) {
        this.bankLocationId = bankLocationId;
    }

    public BankLocation getBankLocation() {
        return bankLocation;
    }

    public void setBankLocation(BankLocation bankLocation) {
        this.bankLocation = bankLocation;
    }
}
