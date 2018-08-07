package com.parabit.mmrbt.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by williamsnyder on 3/12/18.
 */

public class BankLocation {

    public enum LocationType {
        @SerializedName("atm") ATM,
        @SerializedName("branch") BRANCH
    }

    @SerializedName("type")
    private LocationType locationType;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String distance;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    @SerializedName("emergency_contacts")
    private List<EmergencyContact> emergencyContacts;

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double lon) {
        this.longitude = lon;
    }

    public List<EmergencyContact> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(List<EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    public static BankLocation createBranch(String name, String address, String distance) {
        BankLocation location = new BankLocation();
        location.setName(name);
        location.setAddress(address);
        location.setDistance(distance);
        location.setLocationType(LocationType.BRANCH);
        return location;
    }

    public static BankLocation createATM(String name) {
        BankLocation location = new BankLocation();
        location.setName(name);
        location.setLocationType(LocationType.ATM);
        return location;
    }

    public static class EmergencyContact {

        public enum ContactType {
            @SerializedName("fire") FIRE,
            @SerializedName("police") POLICE
        }

        @SerializedName("type")
        private ContactType contactType;
        private String name;
        private String phone;

        public ContactType getContactType() {
            return contactType;
        }

        public void setContactType(ContactType contactType) {
            this.contactType = contactType;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
