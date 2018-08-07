package com.parabit.mmrbt;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by williamsnyder on 4/11/18.
 */

/**
 * BeaconManager is a service to scanning for beacon
 * it can be bind in acticity or start in Application
 * */
public abstract class ParabitBeaconApplication extends Application
        implements BootstrapNotifier, RangeNotifier {

    private static final String TAG = "ParabitBeaconApp";
    private static String IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    private BeaconManager mBeaconManager;

    private Set<Integer> monitoredBeacons = new HashSet<>();

    private BeaconParser iBeaconParser = new BeaconParser().
            setBeaconLayout(IBEACON_LAYOUT);
    private BeaconMonitor mBeaconMonitor;

    private static final String BOOTSTRAP_REGION = "default";

    public static final String NOTIFICATION_CHANNEL_ID_SCANNING = "com.parabit.beacon.scanning";


    public void onCreate() {
        super.onCreate();

        mBeaconMonitor = BeaconMonitor.getInstanceForApplication(getApplicationContext());

        mBeaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        /**
         * Beacon parseer is used to decode the beacon
         * */
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(iBeaconParser);

        /**
         * foreground server to scanner for beacon nearby
         * */
        setupScanning();

        /**
         * auto save energy
         * */
        new BackgroundPowerSaver(this);

        /**
         * use to identify the beaconManager, if we want to stop the scanning, we need to pass the
         * same Region
         *
         * the UUID Here is the same as the quick beacon
         * */
        Region iBeaconRegion = new Region(BOOTSTRAP_REGION,
                Identifier.parse("c4abe711-56a5-fa70-a23f-616263646566"), null, null);

        /**
         * cause background scanning for beacons to start on Android device startup.
         * If a matching beacon is detected, the BootstrapNotifier didEnterRegion method will be called
         *
         * cause beacon scanning to start back up after power is connected or disconnected from a device
         * if the user has force terminated the app
         * */
        new RegionBootstrap(this, iBeaconRegion);

    }

    /**
     * BootstrapNotifier
     *
     * Called with a state value of MonitorNotifier.INSIDE when at least one beacon in a Region is visible.
     * Called with a state value of MonitorNotifier.OUTSIDE when no beacons in a Region are visible.
     * */
    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.d(TAG,"I have just switched from seeing/not seeing :" + region.getUniqueId() +  ":" + state);

        if (state == MonitorNotifier.INSIDE) {
            handleDidEnterRegion(region);
        }
        if (state == MonitorNotifier.OUTSIDE) {
            handleDidExitRegion(region);
        }
    }

    /**
     * RangeNotifier
     * Called once per second to give an estimate of the mDistance to visible beacons
     *
     * we can get the available beacon in this function
     * */
    @Override
    public void didRangeBeaconsInRegion(Collection<org.altbeacon.beacon.Beacon> beacons, Region region) {
        for (org.altbeacon.beacon.Beacon beacon: beacons) {
            handleBeaconInProximity(beacon);
        }
    }


    /**
     * BootstrapNotifier
     * */
    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "did enter region.");
    }

    /**
     * BootstrapNotifier
     * */
    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "did exit region.");
        try {

            if (BOOTSTRAP_REGION.equals(region.getUniqueId())) {
                return;
            }

            Integer serialNumber = getBeaconSerialNumber(region.getId2(), region.getId3());
            if (serialNumber == null) {
                return;
            }
            mBeaconManager.stopMonitoringBeaconsInRegion(region);
            monitoredBeacons.remove(serialNumber);
            mBeaconMonitor.notifyDidExitBeaconRegion(serialNumber);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void handleDidEnterRegion(Region region) {
        try {
            /**
             * Tells the BeaconService to start looking for beacons that match the passed Region object,
             * and providing updates on the estimated mDistance every seconds while beacons in the Region are visible.
             *
             * TODO: difference between startRangingBeaconsInRegion and startMonitoringBeaconsInRegion
             * */
            mBeaconManager.startRangingBeaconsInRegion(region);
            /**
             * add the RangeNotifier to BeaconManager
             * so that we can get notified with the in-range beacon
             * */
            mBeaconManager.addRangeNotifier(this);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void handleDidExitRegion(Region region) {
        try {
            /**
             * Tells the BeaconService to stop looking for beacons that match the passed Region object
             * and providing mDistance information for them.
             * */
            mBeaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * monitor and collect the available beacon
     * notify the BeaconMonitor
     * */
    private void handleBeaconInProximity(Beacon beacon) {
        // get the serialNumber of the beacon
        int serialNumber = getBeaconSerialNumber(beacon.getId2(), beacon.getId3());

        if (monitoredBeacons.contains(serialNumber)) {
            return;
        }

        Region singleBeaconRegion = new Region(beacon.toString(), beacon.getIdentifiers());
        try {
            /**
             * Tells the BeaconService to start looking for beacons that match the passed Region object
             * */
            mBeaconManager.startMonitoringBeaconsInRegion(singleBeaconRegion);
            monitoredBeacons.add(serialNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mBeaconMonitor.notifyDidEnterBeaconRegion(serialNumber);
    }

    private Integer getBeaconSerialNumber(Identifier id2, Identifier id3) {

        if (id2 == null || id3 == null) {
            return null;
        }

        byte[] a = id2.toByteArray();
        byte[] b = id3.toByteArray();

        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        int serialNumber = 0;
        ByteBuffer wrapped = ByteBuffer.wrap(c); // big-endian by default
        wrapped.order( ByteOrder.LITTLE_ENDIAN);
        try {
            serialNumber = wrapped.getInt(); // 1
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return serialNumber;
    }

    private void setupScanning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupOreoScanning();
        } else {
            setupNormalScanning();
        }
    }

    /**
     * foreground scanning
     * can be use at Android 8.0
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupOreoScanning() {
        /**
         * se up a notification for forground scanning (foreground service)
         * */
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_SCANNING, "Beacons",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(true);
        channel.setSound(null, null);
        mNotificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(this,
                NOTIFICATION_CHANNEL_ID_SCANNING);

        int drawableResourceId = getResource("com.parabit.mmrbtsdk.trayIcon");

        builder.setSmallIcon(drawableResourceId);
        builder.setContentTitle("Scanning for Beacons");
        // TODO: why use launch intent here
        Intent intent = getLaunchIntent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        // enable foreground scanning
        mBeaconManager.enableForegroundServiceScanning(builder.build(), 456);
        // Setting this value to false will disable ScanJobs when the app is run on Android 8+, \
        // which can prohibit delivery of callbacks when the app is in the background unless
        // the scanning process is running in a foreground service. This method may only be called if bind()
        // has not yet been called, otherwise an `IllegalStateException` is thown.
        mBeaconManager.setEnableScheduledScanJobs(false);

        // TODO: understand the meaning of this 4 functions
        mBeaconManager.setBackgroundBetweenScanPeriod(0);
        mBeaconManager.setBackgroundScanPeriod(1100);
        mBeaconManager.setForegroundScanPeriod(1125l);
        mBeaconManager.setForegroundBetweenScanPeriod(0);

    }

    private void setupNormalScanning() {
        mBeaconManager.setAndroidLScanningDisabled(false);
        mBeaconManager.setForegroundScanPeriod(1125l);
        mBeaconManager.setBackgroundScanPeriod(1125l);
        mBeaconManager.setForegroundBetweenScanPeriod(0);
        mBeaconManager.setBackgroundBetweenScanPeriod(0);
        mBeaconManager.setRegionStatePersistenceEnabled(false);
    }

    private Intent getLaunchIntent() {
        String packageName = getPackageName();
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        return launchIntent;
    }

    private int getResource(String key) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            int resourceId = bundle.getInt(key);
            return resourceId;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        return 0;
    }

}
