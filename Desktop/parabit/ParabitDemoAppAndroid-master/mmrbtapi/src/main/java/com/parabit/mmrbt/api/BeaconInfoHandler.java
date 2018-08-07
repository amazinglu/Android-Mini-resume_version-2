package com.parabit.mmrbt.api;

/**
 * Created by williamsnyder on 2/21/18.
 */

public interface BeaconInfoHandler {
    void onResult(BeaconInfo beaconInfo);

    void onError(String s);
}
