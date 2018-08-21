package com.parabit.parabeacon.app.demo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.parabit.mmrbt.BeaconListener;
import com.parabit.mmrbt.ParabitBeaconApplication;
import com.parabit.mmrbt.ParabitBeaconSDK;
import com.parabit.mmrbt.api.BankLocation;
import com.parabit.mmrbt.api.BankLocationHandler;
import com.parabit.mmrbt.api.BeaconInfo;
import com.parabit.mmrbt.api.BeaconInfoHandler;
import com.parabit.mmrbt.api.RegistrationHandler;
import com.parabit.parabeacon.app.demo.layout.DoorActivity;
import com.parabit.parabeacon.app.demo.layout.HomeActivity;
import com.parabit.parabeacon.app.demo.manager.AppLogManager;
import com.parabit.parabeacon.app.demo.state.AppState;
import com.parabit.parabeacon.app.demo.state.AppStateManager;
import com.parabit.parabeacon.app.demo.state.Door;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.metrics.MetricsManager;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

/**
 * Created by williamsnyder on 8/12/17.
 */

public class MMRDemoApplication extends ParabitBeaconApplication
        implements BeaconListener, RegistrationHandler {

    private static final String TAG = "MMRDemoApplication";
    private static final int DEMO_NOTIFICATION_ID = 123;
    private static final String NOTIFICATION_CHANNEL_ID_INFO = "com.parabit.beacon.info";

    private AppStateManager appStateManager;

    private Map<String, BeaconInfo> beaconMap = new HashMap<>();

    public void onCreate() {
        super.onCreate();

        appStateManager = new AppStateManager(getApplicationContext());

        /**
         * update the AppState
         * */
        AppState currentState = appStateManager.currentState();
        currentState.clearAvailableDoors();
        currentState.setDemoMode(BuildConfig.BUILD_TYPE.contains("show"));
        appStateManager.update(currentState);
        appStateManager.currentState().addObserver(mStateObserver);

        /**
         * Oauth register -> get the token to access the server
         * Beacon listener
         * */
        ParabitBeaconSDK.register(this, this);
        ParabitBeaconSDK.addBeaconListener(this);

        /**
         *  use for hockey app
         * */
        MetricsManager.register(this);
        MetricsManager.trackEvent("APP_STARTED");
        CrashManager.register(this);

        Mapbox.getInstance(this, "pk.eyJ1Ijoic255ZGVyc2F1cnVzIiwiYSI6ImNqZ3o0NnJldDByc2Qyd2xlYzZmY2NqMnMifQ.bCLjSIcFUa1Z-FX_3TyESA");

        try {
            /**
             * only for test purpose
             * */
            loadBeacons();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> props = new HashMap<>();
        props.put("os","android");
        log().info("App started", props);

    }


    public AppStateManager getAppStateManager() {
        return this.appStateManager;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;

    /**
     * TODO: how is this means
     * */
    private Observer mStateObserver = (o, arg) -> removeNotificationIfNecessary();

    /**
     * post notification for new beacon
     * */
    private void postNotifications(BeaconInfo parabeacon) {
        /**
         * create a new door object base on BeaconInfo
         * */
        Door door = new Door();

        if (parabeacon == null) {
            log().debug("Posting notification. No beacon info provided.");
            door.setUuid("unknown");
            door.setName("Unknown");
            door.setLocation("Unknown");
            door.setSerialNumber("unknown");
        } else {
            log().debug("Posting notification for beacon " + parabeacon.getSerialNumber());
            door.setUuid(parabeacon.getUuid());
            door.setName(parabeacon.getName());
            door.setLocation(parabeacon.getLocation());
            door.setSerialNumber(parabeacon.getSerialNumber());
        }

        /**
         * add the new door object to AppState
         * */
        AppState currentState = appStateManager.currentState();
        currentState.addAvailableDoor(door);
        appStateManager.update(currentState);

        /**
         * start the door activity
         *
         * TODO: when to start the doorActivity
         * */
        if (!isActivityVisible() && !getAppStateManager().currentState().ignoreDoor()) {
            log().debug("Showing fullscreen notification");
            Intent intent = new Intent(this, DoorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            this.startActivity(intent);
        }

        /**
         * build up notification for new beacon
         * */
        showDoorNotification();
    }

    private void showDoorNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder mBuilder = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_INFO, "Beacons",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder = new Notification.Builder(this,
                    NOTIFICATION_CHANNEL_ID_INFO);
        } else {
            mBuilder = new Notification.Builder(this);
        }


        if (isNotificationVisible(mNotificationManager) || getAppStateManager().currentState().ignoreDoor()) {
            return;
        }

        log().debug("Showing system notification.");
        mBuilder.setSmallIcon(R.drawable.pb_bank_building_white_24dp)
                .setContentTitle("MMR Door Available")
                .setContentText("Please tap to open the door")
                .setStyle(new Notification.BigTextStyle()
                        .bigText("We'll guide you through the process for accessing the building."))
                .setAutoCancel(true);
        ;

        Intent resultIntent = new Intent(this, HomeActivity.class);

        /**
         * new task
         * TODO: why need new task here
         * */
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        mNotificationManager.notify(DEMO_NOTIFICATION_ID, mBuilder.build());
    }

    private void removeNotificationIfNecessary() {
        List<Door> doors = getAppStateManager().currentState().getAvailableDoors();
        if (doors == null || doors.size() == 0) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(DEMO_NOTIFICATION_ID);
        }
    }

    private static boolean isNotificationVisible(NotificationManager mNotificationManager) {
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == DEMO_NOTIFICATION_ID) {
                return true;
            }
        }

        return false;
    }

    private Logger log() {
        return AppLogManager.getLogger();
    }

    /**
     * Beacon listener
     * */
    @Override
    public void didEnterBeaconRegion(int serialNumber) {

        log().debug("Beacon detected ("+serialNumber+")");

        // TODO: log: the beacon is detected via bluetooth
        /**
         * send: null
         * response: serial number
         * can get door info: no
         * duration: no
         * */

        if (appStateManager.currentState().isDemoMode()) {
            BeaconInfo beaconInfo = getParabitBeacon(serialNumber);
            postNotifications(beaconInfo);
            return;
        }

        ParabitBeaconSDK.getBeaconInfo(serialNumber, new BeaconInfoHandler() {
            @Override
            public void onResult(BeaconInfo beaconInfo) {
                // TODO log: get back the beacon info base on the serial number
                /**
                 * send: serial number
                 * response: serial number, door name, location, id || response is null
                 * can get door info: yes
                 * duration: yes
                 * */
                if (beaconInfo == null) {
                    Map<String, String> properties = new HashMap<>();
                    properties.put("SERIAL_NUMBER", Integer.toString(serialNumber));
                    MetricsManager.trackEvent("BEACONS_NOT_FOUND",properties);

                    BeaconInfo defaultBeaconInfo = new BeaconInfo();
                    String sn = Integer.toString(serialNumber);
                    defaultBeaconInfo.setSerialNumber(sn);
                    defaultBeaconInfo.setName(sn);
                    defaultBeaconInfo.setLocation("Not registered");
                    defaultBeaconInfo.setUuid(sn);
                    postNotifications(defaultBeaconInfo);
                    return;
                }

                updateEmergencyContacts(beaconInfo);

                postNotifications(beaconInfo);
            }

            @Override
            public void onError(String s) {
                // TODO log: the server fail to return beacon info
                /**
                 * send: serial number
                 * response: fail to get beacon info
                 * can get door info: no
                 * duration: yes
                 * */
                Map<String, String> properties = new HashMap<>();
                properties.put("MESSAGE", s);
                MetricsManager.trackEvent("BEACONS_NOT_FOUND", properties);
                log().error("Unable to get beacon info:"+ s);
            }
        });
    }

    /**
     * Beacon Listener
     * */
    @Override
    public void didExitBeaconRegion(int serialNumber) {
        // TODO log: beacon exist region
        /**
         * send: null
         * response: serial number
         * can get door info: yes
         * duration: no
         * */
        appStateManager.currentState().removeAvailableDoor(Integer.toString(serialNumber));
        appStateManager.update(appStateManager.currentState());
    }

    /**
     * function of RegistrationHandler
     * */
    @Override
    public void onRegistered() {

        // TODO log: get authentication
        /**
         * send: appId
         * response: beaconId, beaconUrl, controlId, controlUrl, locationKey, locationUrl
         * can get door info: no
         * duration: yes
         * */

        // TODO log: register
        /**
         * send: null
         * response: token, DiviceID || repsonse body is null
         * can get door info: no
         * duration: yes
         * */

        MetricsManager.trackEvent("APP_REGISTERED");
        log().debug("App registered");
    }

    @Override
    public void onError(String s) {

        // TODO log: auth or register error

        Map<String, String> properties = new HashMap<>();
        properties.put("MESSAGE", s);
        MetricsManager.trackEvent("REGISTRATION_ERROR", properties);
        log().debug("App not registered:" + s);
    }

    private void updateEmergencyContacts(BeaconInfo beaconInfo) {
        ParabitBeaconSDK.getBankLocation(beaconInfo.getBankLocationId(), new BankLocationHandler() {
            @Override
            public void onResult(BankLocation bankLocation) {
                log().debug("Retrieved bank location for beacon.");
                log().debug("Updating emergency contact info.");
                List<BankLocation.EmergencyContact> contacts = bankLocation.getEmergencyContacts();
                AppState currentState = getAppStateManager().currentState();
                currentState.setContacts(contacts);
                getAppStateManager().currentState().apply(currentState);
            }

            @Override
            public void onError(String s) {
                log().error("Unable to get branch info:"+ s);
            }
        });
    }

    private void loadBeacons() throws Exception{
        InputStream is = getResources().openRawResource(R.raw.beacons);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        String jsonString = writer.toString();

        BeaconInfo[] beacons = new Gson().fromJson(jsonString, BeaconInfo[].class);
        beaconMap.clear();
        if (beacons != null) {
            for (BeaconInfo beacon: beacons) {
                beaconMap.put(beacon.getSerialNumber(), beacon);
            }
        }
    }

    private BeaconInfo getParabitBeacon(int serialNumber) {
        String key = Integer.toString(serialNumber);
        if (beaconMap.containsKey(key)) {
            return beaconMap.get(key);
        }

        BeaconInfo defaultBeaconInfo = new BeaconInfo();
        String sn = Integer.toString(serialNumber);
        defaultBeaconInfo.setSerialNumber(sn);
        defaultBeaconInfo.setName(sn);
        defaultBeaconInfo.setLocation("Not registered");
        defaultBeaconInfo.setUuid(sn);
        return defaultBeaconInfo;

    }
}
