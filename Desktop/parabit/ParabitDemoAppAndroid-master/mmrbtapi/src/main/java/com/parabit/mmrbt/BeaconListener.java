package com.parabit.mmrbt;

/**
 * Created by williamsnyder on 4/11/18.
 */

public interface BeaconListener {

    void didEnterBeaconRegion(int serialNumber);

    void didExitBeaconRegion(int serialNumber);

}
