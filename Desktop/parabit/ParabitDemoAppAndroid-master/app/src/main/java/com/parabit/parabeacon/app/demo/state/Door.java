package com.parabit.parabeacon.app.demo.state;

import com.google.gson.Gson;

import java.util.Objects;

/**
 * Created by williamsnyder on 8/29/17.
 */

public class Door {

    private String name;
    private String location;
    private String uuid;
    private String serialNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String toJSON() {
        return new Gson().toJson(this).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Door door = (Door) o;
        return Objects.equals(name, door.name) &&
                Objects.equals(location, door.location) &&
                Objects.equals(uuid, door.uuid) &&
                Objects.equals(serialNumber, door.serialNumber);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, location, uuid, serialNumber);
    }
}
