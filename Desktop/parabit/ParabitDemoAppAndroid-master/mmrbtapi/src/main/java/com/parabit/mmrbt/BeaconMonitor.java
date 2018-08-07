package com.parabit.mmrbt;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by williamsnyder on 4/11/18.
 */

public class BeaconMonitor {

    private static BeaconMonitor instance;
    // TODO: why there car be multiple beaconListeners
    private Set<BeaconListener> beaconListeners = new HashSet<>();
    private Set<Integer> beaconsInRange = new HashSet<>();

    private BeaconMonitor(Context context) {
    }

    public static  BeaconMonitor getInstanceForApplication(Context context) {
        if (instance == null) {
            instance = new BeaconMonitor(context);
        }

        return instance;
    }

    public void addBeaconNotifier(BeaconListener beaconListener) {
        if (beaconListener != null) {
            beaconListeners.add(beaconListener);
        }
    }

    public void removeBeaconNotifier(BeaconListener beaconListener) {
        if (beaconListener != null) {
            beaconListeners.remove(beaconListener);
        }
    }

    /**
     * this function will be called when there is new beacon discovered
     *
     * tell all the beacon listeners that there is a new beacon
     * */
    void notifyDidEnterBeaconRegion(int serialNumber) {
        beaconsInRange.add(serialNumber);
        for (BeaconListener notifier: beaconListeners) {
            notifier.didEnterBeaconRegion(serialNumber);
        }
    }

    void notifyDidExitBeaconRegion(int serialNumber) {
        beaconsInRange.remove(serialNumber);
        for (BeaconListener notifier: beaconListeners) {
            notifier.didExitBeaconRegion(serialNumber);
        }
    }

    public Set<Integer> getBeaconsInRange() {
        return beaconsInRange;
    }
}
