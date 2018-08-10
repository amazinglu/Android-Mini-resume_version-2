// Copyright 2016 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.parabit.parabeacon.app.tech;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parabit.beacon.ParabitFirmwareManager;
import com.parabit.beacon.api.BeaconInfo;
import com.parabit.beacon.firmware.FirmwareSummary;
import com.parabit.beacon.firmware.FirmwareUnlockResponse;
import com.parabit.parabeacon.app.tech.dialogs.ConfirmationDialog;
import com.parabit.parabeacon.app.tech.dialogs.SaveConfigurationDialog;
import com.parabit.parabeacon.app.tech.dialogs.UnlockDialog;
import com.parabit.parabeacon.app.tech.gatt.GattClient;
import com.parabit.parabeacon.app.tech.gatt.GattClientException;
import com.parabit.parabeacon.app.tech.gatt.GattConstants;
import com.parabit.parabeacon.app.tech.gatt.GattOperationException;
import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;
import com.parabit.parabeacon.app.tech.utils.BroadcastCapabilities;
import com.parabit.parabeacon.app.tech.utils.UiUtils;
import com.parabit.parabeacon.app.tech.utils.Utils;

import layout.BeaconTabFragment;
import layout.DFUFragment;
import layout.GlobalFragment;
import layout.SlotFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Activity for connecting to the beacon, unlocking it, reading its characteristics
 *  and configuring them.
 *
 *  we have three kinds of UI here to set up the beacon
 *  GlobalFragment
 *  DFUFragment => update the firmware of the beacon
 *  SlotFragment
 */
public class BeaconConfigActivity extends SessionActivity
        implements SlotFragment.ConfigurationListener {
    private static final String TAG = BeaconConfigActivity.class.getSimpleName();

    private BroadcastCapabilities capabilities;
    private GattClient gattClient;
    private String unlockCode;
    private String address;
    private String name;
    private String rssi;

    private String title;
    private String subtitle;

    private String firmwareVersion;
    private String hardwareVersion;
    private String serialNumber;
    private String manufacturer;
    private String model;
    private BeaconInfo info;
    private Gson gson;

    private SlotsAdapter slotsAdapter;
    private ViewPager viewPager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean intendedDisconnection = false;

    private ExecutorService executor;
    private SavedConfigurationsManager configurationsManager;
    private FirmwareSummary firmware;
    private ParabitFirmwareManager firmwareService;
    private int selectedTab;

    public enum BeaconTabMap {
        GENERAL,
        CONFIG,
        DFU
    }

    /**
     * this activity is try to access to a specific door (beacon)
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_config);

        this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        name = getIntent().getStringExtra(BeaconListAdapter.ViewHolder.BEACON_NAME);
        address = getIntent().getStringExtra(BeaconListAdapter.ViewHolder.BEACON_ADDRESS);
        rssi = getIntent().getStringExtra(BeaconListAdapter.ViewHolder.BEACON_RSSI);
        title = getIntent().getStringExtra(BeaconListAdapter.ViewHolder.BEACON_TITLE);
        subtitle = getIntent().getStringExtra(BeaconListAdapter.ViewHolder.BEACON_SUBTITLE);

        String infoString = getIntent().getStringExtra(BeaconListAdapter.ViewHolder.BEACON_INFO);
        if (infoString != null) {
            info = gson.fromJson(infoString, BeaconInfo.class);
        }

        setupToolbar(name, address);
        setUpThrobber();
        executor = Executors.newSingleThreadExecutor();
        configurationsManager = new SavedConfigurationsManager(this);

        findViewById(R.id.grey_out_slot).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return swipeRefreshLayout.isRefreshing();
            }
        });

        // TODO: what is Firmware use for
        String token = getAuthManager().getFirmwareToken();
        String url = getAuthManager().getFirmwareURL();

        try {
            firmwareService = ParabitFirmwareManager.getInstance(url, token);
        } catch (Exception e) {
            handleFirmwareAPIUnavailable(e);
        }

        accessBeacon();

    }

    /**
     * access door process
     *
     * Starts the process of connecting to the beacon, discovering its services, unlocking it and
     * reading all available information from it.
     *
     * access door process:
     * connect to the beacon, discovering the service,
     * unlock the door and read or edit the information
     */
    public void accessBeacon() {
        disableDisplay();
        executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * the address in the device address
                         * */
                        connectToGatt(address);
                    }
                });
        //if connected, discovering devices takes place in onGattConnected() in the GattListener
    }

    public void reconnectToGatt(BeaconConfigActivity.BeaconTabMap selectedTab) {
        this.selectedTab = selectedTab.ordinal();
        connectToGatt(address);
    }

    public void reconnectToGatt() {
        this.selectedTab = 0;
        connectToGatt(address);
    }

    private void handleFirmwareAPIUnavailable(Exception e) {
        ApplicationLogger.logError(Events.API_FAILURE,e);
        showPopupMessage("Unable to access Firmware service.");
    }

    /**
     * access door process => connect door:
     *
     * connect to the beacon using Gatt
     * using beacon address
     * */
    private void connectToGatt(String address) {
        Log.d(TAG, "Connecting...");
        UiUtils.showToast(this, "Connecting...");
        gattClient = new GattClient(getApplicationContext(), address, gattListener);
        if (gattClient == null) {
            return;
        }
        gattClient.connect();
    }

    /**
     * access door process => connect door:
     *
     * define what to do when Gatt connect and when device is discovered
     * when the door is connected, try to get the service of the beacon
     * when the service is discovered, we can access the door using the service
     * */
    private GattClient.GattListener gattListener = new GattClient.GattListener() {
        @Override
        public void onGattConnected() {
            Log.d(TAG, "Connected to GATT service.");
            /**
             * when beacon is connected, we try to discover the service of the beacon
             * talk with Gatt
             * */
            gattClient.discoverServices();
        }

        @Override
        public void onGattServicesDiscovered() {
            Log.d(TAG, "Discovered GATT services.");
            if (gattClient.isEddystoneGattServicePresent() && gattClient.isDeviceIdServicePresent()) {
                try {
                    /**
                     * access door process => connect door:
                     * after we got all the info of the beacon service
                     * try to unlock and change the setting of the beacon
                     * */
                    byte[] lockState = gattClient.readLockState();
                    if (lockState[0] == GattConstants.LOCK_STATE_LOCKED) {
                        /**
                         * access door process => unlock door
                         * */
                        unlock();
                    } else {
                        /**
                         * access door process => set up beacon information
                         *
                         * show the beacon (door) setting screen
                         * */
                        setupBeaconInformationDisplay();
                    }

                } catch (GattClientException e) {
                    Log.e(TAG, "Gatt Client Error when discovering devices", e);
                    displayConnectionFailScreen("Something went wrong when discovering services "
                            + "of beacon");
                } catch (GattOperationException e) {
                    Log.e(TAG, "Gatt Operation Error when discovering devices", e);
                    displayConnectionFailScreen("Something went wrong when discovering services "
                            + "of beacon");
                }
            } else {
                gattClient.disconnect();
            }
        }

        @Override
        public void onGattDisconnected() {
            Log.d(TAG, "Beacon disconnected.");
            ApplicationLogger.logEvent(Events.BEACON_DISCONNECTED);
            if (intendedDisconnection) {
                UiUtils.showToast(BeaconConfigActivity.this, "Beacon disconnected.");
                intendedDisconnection = false;
            } else if (!gattClient.isEddystoneGattServicePresent()){
                displayConnectionFailScreen("This beacon does not support the GATT service "
                        + "and cannot be configured with this app. \n \n");
            } else {

                if (isDoingDFU()) {
                    return;
                }

                displayConnectionFailScreen("Connection to beacon was lost unexpectedly. \n \n"
                        + getResources().getString(R.string.connect_to_beacon_message));
            }
        }
    };

    private boolean isDoingDFU() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f instanceof DFUFragment) {
                return ((DFUFragment)f).isDoingUpdate();
            }
        }


        return false;
    }

    /**
     * Starts the process of unlocking a beacon. First it tries to automatically unlock the beacon
     * by applying the most common lock codes like all "f"s or all "0"s. If this fails, it pops up
     * a dialog to ask the user for the lock code of the beacon.
     */
    private void unlock() {
        final Runnable setupBeaconInformationDisplay = new Runnable() {
            @Override
            public void run() {
                setupBeaconInformationDisplay();
            }
        };

        try {
            this.firmwareVersion = new String(gattClient.readFirmwareVersion());
        } catch (GattClientException e) {
            Map<String, String> properties = new HashMap<>();
            properties.put(Keys.SERIAL_NUMBER, serialNumber);
            ApplicationLogger.logDebug(Events.ERROR_READING_FIRMWARE, properties, e);
        } catch (GattOperationException e) {
            Map<String, String> properties = new HashMap<>();
            properties.put(Keys.SERIAL_NUMBER, serialNumber);
            ApplicationLogger.logDebug(Events.ERROR_READING_FIRMWARE, properties, e);
        }

        /**
         * access door process => unlock process
         *
         * use the firmware version to get back the firmware summary
         * request with AWS
         *
         * firmware is the firmware summary
         * */
        firmwareService.getFirmwareSummary(this.firmwareVersion,
                new Callback<FirmwareSummary>() {
             @Override
             public void onResponse(Call<FirmwareSummary> call, Response<FirmwareSummary> response) {
                 firmware = response.body();
                 if (firmware == null) {
                     Map<String, String> properties = new HashMap<>();
                     properties.put(Keys.CURRENT_FIRMWARE,firmwareVersion);
                     ApplicationLogger.logDebug(Events.FIRMWARE_NOT_FOUND, properties);
                 }
                 attemptAutomaticUnlock(setupBeaconInformationDisplay);
             }

             @Override
             public void onFailure(Call<FirmwareSummary> call, Throwable t) {
                 Map<String, String> properties = new HashMap<>();
                 properties.put(Keys.SERIAL_NUMBER, serialNumber);
                 properties.put(Keys.CURRENT_FIRMWARE, firmwareVersion);
                 ApplicationLogger.logDebug(Events.API_FAILURE, properties, t);
                 attemptAutomaticUnlock(setupBeaconInformationDisplay);
             }
         });




    }

    private void attemptAutomaticUnlock(final Runnable runnable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {

                ApplicationLogger.logEvent(Events.UNLOCK_ATTEMPT);

                if (firmware == null || firmware.getCurrent() == null) {
                    displayBeaconFirmwareUnrecognized();
                    return;
                }

                try {

                    final String firmwareRevision = firmware.getCurrent().getRevision();
                    byte[] unlockChallenge = gattClient.readUnlock();
                    /**
                     * access door process => unlock process
                     *
                     * use firmwareRevision and unlockChallenge to get challengeResponse
                     * request with AWS
                     * */
                    firmwareService.getUnlockResponse(firmwareRevision, unlockChallenge,
                            new Callback<FirmwareUnlockResponse>() {
                        @Override
                        public void onResponse(Call<FirmwareUnlockResponse> call,
                                               Response<FirmwareUnlockResponse> response) {
                            FirmwareUnlockResponse unlockResponse = response.body();
                            String challengeResponse = null;
                            if (unlockResponse != null) {
                                challengeResponse = unlockResponse.getUnlockResponse();
                                Log.d("UNLOCK", "" + challengeResponse);
                            }

                            if (challengeResponse == null ){
                                Map<String, String> properties = new HashMap<>();
                                properties.put(Keys.CURRENT_FIRMWARE,firmwareRevision);
                                properties.put(Keys.REQUEST_DURATION,
                                        Long.toString(response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis()));
                                ApplicationLogger.logError(Events.UNLOCK_CODE_NOT_FOUND, properties);
                                Log.d(TAG, "unable to get unlock code, challenge null");
                                displayBeaconUnlockFailed();
                            }


                            boolean unlocked = false;
                            boolean didError = false;
                            try {
                                /**
                                 *  access door process => unlock process
                                 *
                                 * use Challenge response to unlock the door
                                 * using Gatt
                                 * */
                                unlocked = gattClient.unlockWithChallengeResponse(
                                        Utils.toByteArray(challengeResponse));
                            } catch (Exception ex) {
                                didError = true;
                                Log.d(TAG, "unable to get unlock beacon");
                                Map<String, String> properties = new HashMap<>();
                                properties.put("FIRMWARE_REVISION",firmwareRevision);
                                properties.put(Keys.REQUEST_DURATION,
                                        Long.toString(response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis()));
                                ApplicationLogger.logError("UNLOCK_FAILED", properties, ex);
                                displayBeaconUnlockFailed();
                            }

                            if (unlocked) {
                                Log.d(TAG, "Beacon unlocked automatically");
                                Map<String, String> properties = new HashMap<>();
                                properties.put("FIRMWARE_REVISION",firmwareRevision);
                                properties.put(Keys.REQUEST_DURATION,
                                        Long.toString(response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis()));
                                ApplicationLogger.logEvent("UNLOCK_SUCCESSFUL",properties);
                                runnable.run();
                            } else if (!didError){
                                Map<String, String> properties = new HashMap<>();
                                properties.put("FIRMWARE_REVISION",firmwareRevision);
                                properties.put(Keys.REQUEST_DURATION,
                                        Long.toString(response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis()));
                                ApplicationLogger.logError("UNLOCK_FAILED", properties);
                                displayBeaconUnlockFailed();
                            }
                        }

                        @Override
                        public void onFailure(Call<FirmwareUnlockResponse> call, Throwable t) {
                            Log.d(TAG, "unable to get unlock response");
                            Map<String, String> properties = new HashMap<>();
                            properties.put(Keys.CURRENT_FIRMWARE,firmwareRevision);
                            ApplicationLogger.logError(Events.UNLOCK_CLOUD_RESPONSE_FAILURE, properties, t);
                            displayBeaconUnlockFailed();
                        }
                    });
                } catch (GattClientException e) {
                    Map<String, String> properties = new HashMap<>();
                    properties.put(Keys.CURRENT_FIRMWARE,firmwareVersion);
                    ApplicationLogger.logError(Events.UNLOCK_WRITE_FAILED, properties, e);
                    Log.d(TAG, "unable to get unlock response");
                    displayBeaconUnlockFailed();
                } catch (GattOperationException e) {
                    Map<String, String> properties = new HashMap<>();
                    properties.put(Keys.CURRENT_FIRMWARE,firmwareVersion);
                    ApplicationLogger.logError(Events.UNLOCK_WRITE_FAILED, properties, e);
                    Log.d(TAG, "unable to get unlock response");
                    displayBeaconUnlockFailed();
                }

                //if automatic unlock fails, a dialog pops up to ask the user for the beacon's lock
                // code
                //attemptManualUnlock(runnable);
            }
        });
    }

    private void displayBeaconUnlockFailed(){
        displayConnectionFailScreen("We were unable to unlock the beacon. \n \n"
                + "It has to be unlocked before accessing its configuration.  \n \n" + "If the problem persists, please contact support. \n \n"
//                "Firmware version: " + firmwareVersion + " \n \n" +
//                "Serial number: " + serialNumber
            );
    }

    private void displayBeaconFirmwareUnrecognized(){
        Map<String, String> props = new HashMap<>();
        props.put(Keys.CURRENT_FIRMWARE,firmwareVersion);
        props.put(Keys.SERIAL_NUMBER,serialNumber);
        ApplicationLogger.logError(Events.FW_UNKNOWN, props);
        displayConnectionFailScreen("We were unable to unlock the beacon. \n \n"
                        + "The current firmware is unsupported.  \n \n" + "Please contact support. \n \n" +
                "Firmware version: " + firmwareVersion + " \n \n"
//                "Serial number: " + serialNumber
        );
    }

    private void attemptManualUnlock(final Runnable runnable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                UnlockDialog.show(BeaconConfigActivity.this, new UnlockDialog.UnlockListener() {
                    @Override
                    public void unlockingDismissed() {
                        displayConnectionFailScreen("This beacon is locked. \n \n"
                                + "It has to be unlocked before accessing its characteristics.");
                    }

                    @Override
                    public void unlock(byte[] unlockCode) {
                        String unlockCodeString = Utils.toHexString(unlockCode);
                        Log.d(TAG, "Trying " + unlockCodeString);
                        if (gattClient.unlock(unlockCode)) {
                            BeaconConfigActivity.this.unlockCode = unlockCodeString;
                            if (runnable != null) {
                                runnable.run();
                            }
                        } else {
                            UiUtils.showToast(BeaconConfigActivity.this,
                                    "Incorrect lock code. Try again?");
                            attemptManualUnlock(runnable);
                        }
                    }
                });
            }
        });
    }

    /**
     * Displays a screen with a message and a "try again" button. Pressing the button will attempt
     * to connect to the beacon from beginning
     *
     * @param message message which we want to be printed on the screen
     */
    private void displayConnectionFailScreen(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enableDisplay();
                ViewGroup configContentView
                        = (ViewGroup) findViewById(R.id.beacon_config_page_content);
                UiUtils.makeChildrenInvisible(configContentView);
                ViewGroup connectionFailSlot = (ViewGroup) findViewById(R.id.connection_fail);
                connectionFailSlot.findViewById(R.id.connection_fail_btn)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                accessBeacon();
                                findViewById(R.id.connection_fail).setVisibility(View.GONE);
                            }
                        });
                ((TextView) connectionFailSlot.findViewById(R.id.connection_fail_message))
                        .setText(message);
                connectionFailSlot.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * access door process => setup the beacon
     *
     * Called after successful connection to the beacon is made and services are
     * discovered successfully.
     *
     * It sets up the beacon configuration screen with tabs which are controlled by a view pager and
     * an adapter.
     */
    private void setupBeaconInformationDisplay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewPager = (ViewPager) findViewById(R.id.view_pager);
                slotsAdapter = new SlotsAdapter(getSupportFragmentManager());
                viewPager.setAdapter(slotsAdapter);

                TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
                tabs.setupWithViewPager(viewPager);
                //tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
                //tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

                findViewById(R.id.appbar_rssi_info).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.appbar_rssi_value)).setText(rssi + " db");

                setUpFragments();
            }
        });
    }

    /**
     * Reads information about the beacon from the gatt client. It creates a fragment
     * for each slot of the beacon and puts it in the tab layout. Then all the available information
     * about this slot is read and displayed on the screen
     *
     * All operations do not involve UI are done on the background thread
     */
    private void setUpFragments() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    /**
                     * read the info from the gattClient
                     * */
                    byte[] fwVersion = gattClient.readFirmwareVersion();
                    firmwareVersion = new String(fwVersion);
                    Log.d(TAG, "DI:firmware:" + firmwareVersion);

                    byte[] hwVersion = gattClient.readHardwareVersion();
                    hardwareVersion = new String(hwVersion);
                    Log.d(TAG, "DI:hardware:" + hardwareVersion);

                    byte[] sNumber = gattClient.readSerialNumber();
                    serialNumber = new String(sNumber);
                    Log.d(TAG, "DI:serial:" + serialNumber);

                    byte[] mn = gattClient.readManufacturer();
                    manufacturer = new String(mn);
                    Log.d(TAG, "DI:manu:" + manufacturer);

                    byte[] md = gattClient.readModel();
                    model = new String(md);
                    Log.d(TAG, "DI:model:" + model);

                    byte[] data = gattClient.readBroadcastCapabilities();
                    capabilities = new BroadcastCapabilities(data);

                    Bundle globalDataBundle = new Bundle();
                    globalDataBundle.putByteArray(Constants.BROADCAST_CAPABILITIES, data);
                    globalDataBundle.putString(Constants.BEACON_ADDRESS, address);
                    globalDataBundle.putString(Constants.BEACON_NAME, name);

                    globalDataBundle.putString(Constants.BEACON_FW, firmwareVersion);
                    globalDataBundle.putString(Constants.BEACON_HW, hardwareVersion);
                    globalDataBundle.putString(Constants.BEACON_SN, serialNumber);
                    globalDataBundle.putString(Constants.BEACON_MN, manufacturer);
                    globalDataBundle.putString(Constants.BEACON_MD, model);

                    byte[] remainConnectable = gattClient.readRemainConnectable();
                    globalDataBundle.putByte(Constants.REMAIN_CONNECTABLE, remainConnectable[0]);

                    /**
                     * TODO: what is the capabilities means
                     * capabilities hold the character of the beacon
                     * */
                    if (!capabilities.isVariableTxPowerSupported()) {
                        String radioTxPower = Integer.toString(gattClient.readRadioTxPower()[0]);
                        String advTxPower = Integer.toString(gattClient.readAdvertisedTxPower()[0]);
                        globalDataBundle.putString(Constants.TX_POWER, radioTxPower);
                        globalDataBundle.putString(Constants.ADV_POWER, advTxPower);
                    }
                    if (!capabilities.isVariableAdvSupported()) {
                        String advInterval
                                = Integer.toString(Utils.toInt
                                (gattClient.readAdvertisingInterval()));
                        globalDataBundle.putString(Constants.ADV_INTERVAL, advInterval);
                    }

                    /**
                     * put GlobalFragment into view pager
                     * */
                    GlobalFragment globalFragment = GlobalFragment.newInstance(globalDataBundle);
                    slotsAdapter.addFragment(globalFragment);

                    /**
                     * TODO: what is the getMaxSupportedTotalSlots(), why is 1 here
                     *
                     * something with the slot
                     * */
                    for (int i = 0; i < 1/*capabilities.getMaxSupportedTotalSlots()*/; i++) {
                        gattClient.writeActiveSlot(i);
                        final Bundle slotInfoBundle = new Bundle();
                        slotInfoBundle.putString(Constants.BEACON_SN, serialNumber);

                        final byte[] slotData = gattClient.readAdvSlotData();
                        slotInfoBundle.putByteArray(Constants.SLOT_DATA, slotData);
                        globalDataBundle.putByteArray(Constants.SLOT_DATA, slotData);

                        if (globalDataBundle.containsKey(Constants.ADV_INTERVAL)){
                            slotInfoBundle.putString(Constants.ADV_INTERVAL, globalDataBundle.getString(Constants.ADV_INTERVAL));
                        };

                        if (capabilities.isVariableTxPowerSupported()) {
                            String radioTxPower = Integer.toString
                                    (gattClient.readRadioTxPower()[0]);
                            String advTxPower = Integer.toString
                                    (gattClient.readAdvertisedTxPower()[0]);
                            slotInfoBundle.putString(Constants.TX_POWER, radioTxPower);
                            slotInfoBundle.putString(Constants.ADV_POWER, advTxPower);
                        }

                        if (capabilities.isVariableAdvSupported()) {
                            String advInterval = Integer.toString
                                    (Utils.toInt(gattClient.readAdvertisingInterval()));
                            slotInfoBundle.putString(Constants.ADV_INTERVAL, advInterval);
                        }

                        slotInfoBundle.putByteArray(Constants.BROADCAST_CAPABILITIES, data);

                        slotInfoBundle.putInt(Constants.SLOT_NUMBER, i);

                        /**
                         * put SlotFragment into view page
                         * */
                        slotsAdapter.createNewFragment(slotInfoBundle);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                slotsAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    globalDataBundle.putInt(Constants.SLOT_NUMBER, 0);

                    if (firmware != null) {
                        globalDataBundle.putString(Constants.FIRMWARE_UPDATE_URL, firmware.getLatestURL());
                        globalDataBundle.putString(Constants.FIRMWARE_UPDATE_REVISION, firmware.getLatest().getRevision());
                    }

                    /**
                     * put DFUFragment into view page
                     *
                     * for DFU update
                     * */
                    final DFUFragment dfuFragment = DFUFragment.newInstance(globalDataBundle);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            slotsAdapter.addFragment(dfuFragment);
                            slotsAdapter.notifyDataSetChanged();
                        }
                    });

//                    final BeaconRegistrationFragment registrationFragmentFragment =
//                            BeaconRegistrationFragment.newInstance(globalDataBundle);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            slotsAdapter.addFragment(registrationFragmentFragment);
//                            slotsAdapter.notifyDataSetChanged();
//                        }
//                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewPager.setOffscreenPageLimit(
                                    capabilities.getMaxSupportedTotalSlots());
                            setTabTitles();
                            enableDisplay();
                            findViewById(R.id.tab_layout).setVisibility(View.VISIBLE);
                        }
                    });
                } catch (GattClientException e) {
                    Log.d(TAG, "Gatt Client Error when getting information from beacon", e);
                    displayConnectionFailScreen("Something went wrong when getting information "
                            + "from beacon");
                } catch (GattOperationException e) {
                    Log.d(TAG, "Gatt Operation Error when getting information from beacon", e);
                    displayConnectionFailScreen("Something went wrong when getting information "
                            + "from beacon");
                }

            }
        });
    }

    private void setTabTitles() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TabLayout) findViewById(R.id.tabs)).getTabAt(0).setText("GENERAL");
                TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
                for (int i = 1; i < tabs.getTabCount(); i++) {
                    tabs.getTabAt(i).setText(slotsAdapter.getItem(i).name);
                }
            }
        });
    }

    private void setUpThrobber() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.throbber);
                swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                        android.R.color.holo_green_light,
                        android.R.color.holo_orange_light,
                        android.R.color.holo_red_light);
                swipeRefreshLayout.setEnabled(false);
            }
        });
    }

    private void setupToolbar(final String name, final String address) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toolbar.setSubtitle(address);
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                setTitle(name);

                if (title != null) {
                    setTitle(title);
                }

                if (subtitle != null) {
                    toolbar.setSubtitle(subtitle);
                }
            }
        });
    }

    /**
     * Attempts to save all the changes that the user has introduced to any of the beacon's slots
     * by writing values to the gatt client
     *
     * Runs on the background thread
     */
    private void saveChangesAcrossAllTabs() {
        disableDisplay();

        Utils.hideKeyboard(this, findViewById(R.id.tab_layout));

        final Runnable saveAllChangesTask = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < slotsAdapter.getCount(); i++) {
                    slotsAdapter.getItem(i).saveChanges();
                }
            }
        };

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (areAllSlotsAreEmpty()) {
                    ConfirmationDialog.confirm("Save empty beacon", "You are about to configure "
                            + "this beacon to broadcast empty frame from all of its slots. This "
                            + "means that you will not be able to detect this beacon with this "
                            + "app anymore and will not be able to configure it. \n\n Are you "
                            + "sure you want to do this?", "YES", "NO", BeaconConfigActivity.this,
                            new ConfirmationDialog.ConfirmationListener() {
                                @Override
                                public void confirm() {
                                    saveAllChangesTask.run();
                                }

                                @Override
                                public void cancel() {

                                }
                            });
                } else {
                    saveAllChangesTask.run();
                }


                enableDisplay();
                UiUtils.showToast(BeaconConfigActivity.this,
                        getResources().getString(R.string.changes_save_successful));
            }
        });
    }

    private boolean areAllSlotsAreEmpty() {
        for (int i = 1; i < slotsAdapter.getCount(); i++) {
            if (!((SlotFragment) slotsAdapter.getItem(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Starts the process of saving the configuration of the connected beacon on a background
     * thread. It pops a dialog asking the user to enter a name under which the current
     * configuration will be saved. After this the user will be able to choose this name to apply
     * this configuration to any beacon.
     */
    private void saveCurrentConfiguration() {
        disableDisplay();

        SaveConfigurationDialog.show(this, new SaveConfigurationDialog.SaveConfigListener() {
            @Override
            public void configNameChosen(String configName) {
                saveCurrentConfigurationWithName(configName);
            }
        });
    }

    /**
     * Saves the current configuration of the connected beacon. This includes information about all
     * slots - slotData, tx power, adv tx power, adv interval.
     *
     * All of this is done on a background thread
     *
     * @param configName name of the new configuration. Has to be unique.The user will have to
     *                   choose this name whenever he wants to apply this configuration to a beacon
     */
    private void saveCurrentConfigurationWithName(final String configName) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                BeaconConfiguration currentConfiguration = new BeaconConfiguration(configName);

                GlobalFragment globalFragment = (GlobalFragment) slotsAdapter.getItem(0);

                for (int i = 0; i < capabilities.getMaxSupportedTotalSlots(); i++) {
                    SlotFragment currFragment = (SlotFragment) slotsAdapter.getItem(i + 1);
                    if (!currFragment.isEmpty()) {
                        byte[] newSlotData = currFragment.buildNewSlotDataInfo();

                        String txPowerString;
                        String advPowerString;
                        String advIntervalString;
                        if (capabilities.isVariableTxPowerSupported()) {
                            txPowerString = currFragment.getTxPower();
                            advPowerString = currFragment.getAdvTxPower();
                        } else {
                            txPowerString = globalFragment.getTxPower();
                            advPowerString = globalFragment.getAdvTxPower();
                        }

                        if (capabilities.isVariableAdvSupported()) {
                            advIntervalString = currFragment.getAdvInterval();
                        } else {
                            advIntervalString = globalFragment.getAdvInterval();
                        }

                        currentConfiguration.addSlot(newSlotData, Integer.parseInt(txPowerString),
                                Integer.parseInt(advPowerString),
                                Integer.parseInt(advIntervalString));
                    }

                }

                configurationsManager.saveNewConfiguration(currentConfiguration);

                enableDisplay();
                UiUtils.showToast(BeaconConfigActivity.this,
                        "Configuration saved successfully");
            }
        });
    }

    /**
     * Starts the process of applying a saved configuration to the current beacon. This pops up a
     * dialog which shows the names of all saved configurations. The user can choose one of those
     * and it will be applied and saved to current beacon
     */
    private void applyConfiguration() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Apply Configuration");
        final ArrayList<String> configNames = configurationsManager.getConfigurationNamesList();
        b.setItems(configNames.toArray(new String[configNames.size()]),
                new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                applyConfigurationWithName(configNames.get(which));
                dialog.dismiss();
            }
        });
        b.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                enableDisplay();
            }
        });

        b.show();


    }

    /**
     * Applies the saved configuration with name "configName" to the current beacon
     *
     * @param configName the name of the configuration which the user wants to apply to the current
     *                   beacon. It has to be the name of a configuration which the user has
     *                   previously saved. If a configuration with this name does not exist, this
     *                   method does nothing.
     */
    private void applyConfigurationWithName(final String configName) {
        disableDisplay();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                BeaconConfiguration config = configurationsManager.getConfiguration(configName);

                if (config == null) {
                    return;
                }

                // These three are used only when per slot tx power and adv interval are not
                // supported. They will record the highest value of the relevant variable
                // out of all slots and then set that as the global value.
                int maxAdvInterval = Integer.MIN_VALUE;
                int maxTxPower = Integer.MIN_VALUE;
                int maxAdvTxPower = Integer.MIN_VALUE;
                for (int i = 0; i < capabilities.getMaxSupportedTotalSlots(); i++) {
                    SlotFragment slotFragment = (SlotFragment) slotsAdapter.getItem(i+1);
                    if (i >= config.getNumberOfConfiguredSlots()) {
                        // There are more available slots on this beacon than the saved
                        // configuration has defined. Configuring them as empty.
                        if (!slotFragment.isEmpty()) {
                            Log.d(TAG, "Configuring empty slot " + i);
                            slotDataChanged(i, new byte[0]);
                        }
                        continue;
                    }

                    slotDataChanged(i, config.getSlotDataForSlot(i));

                    if (capabilities.isVariableTxPowerSupported()) {
                        slotFragment.setTxPower(config.getRadioTxPowerForSlot(i));
                        slotFragment.setAdvTxPower(config.getAdvTxPowerForSlot(i));
                    } else {
                        if (config.getRadioTxPowerForSlot(i) > maxTxPower) {
                            maxTxPower = config.getRadioTxPowerForSlot(i);
                        }

                        if (config.getAdvTxPowerForSlot(i) > maxAdvTxPower) {
                            maxAdvTxPower = config.getAdvTxPowerForSlot(i);
                        }
                    }

                    if (capabilities.isVariableAdvSupported()) {
                        slotFragment.setAdvInterval(config.getAdvIntervalForSlot(i));
                    } else if (config.getAdvIntervalForSlot(i) > maxAdvInterval) {
                        maxAdvInterval = config.getAdvIntervalForSlot(i);
                    }
                }

                GlobalFragment globalFragment = (GlobalFragment) slotsAdapter.getItem(0);
                if (!capabilities.isVariableTxPowerSupported()) {
                    globalFragment.setTxPower(maxTxPower);
                    globalFragment.setAdvTxPower(maxAdvTxPower);
                }

                if (!capabilities.isVariableAdvSupported()) {
                    globalFragment.setAdvInterval(maxAdvInterval);
                }

                saveChangesAcrossAllTabs();
            }
        });
    }

    /**
     * Stops the swipeRefreshLayout from rotating (if it was rotating at the time of call) and
     * disables all click events on the screen until a call to enableDisplay() is made.
     *
     * Runs on the UI thread
     */
    public void disableDisplay() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.grey_out_slot).setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Enables the display after it has been previously disabled by disableDisplay()
     *
     * Runs on the UI thread
     */
    public void enableDisplay() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.grey_out_slot).setVisibility(View.GONE);

                if (viewPager != null && viewPager.getChildCount() <= selectedTab) {
                    viewPager.setCurrentItem(selectedTab);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_beacon_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.save_changes:
                saveChangesAcrossAllTabs();
                return false;
            case R.id.save_configuration:
                saveCurrentConfiguration();
                return false;
            case R.id.apply_configuration:
                applyConfiguration();
                return false;
            case R.id.manage_configurations:
                Intent intent = new Intent(this, ManageConfigurationsActivity.class);
                startActivity(intent);
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * If the current beacon has unsaved changes and the user presses the back button, this will pop
     * up a dialog asking whether to save the changes or not.
     */
    @Override
    public void onBackPressed() {
        if (tabChangesPending()) {
            ConfirmationDialog.confirm("Save Changes", "There are some "
                            + "unsaved changes to this slot. \n \n Would you like to "
                            + "save those changes?", "SAVE", "DISCARD", BeaconConfigActivity.this,
                    new ConfirmationDialog.ConfirmationListener() {

                        @Override
                        public void confirm() {
                            saveChangesAcrossAllTabs();
                            intendedDisconnection = true;
                            BeaconConfigActivity.super.onBackPressed();
                        }

                        @Override
                        public void cancel() {
                            intendedDisconnection = true;
                            BeaconConfigActivity.super.onBackPressed();
                        }
                    });
        } else {
            super.onBackPressed();
        }
    }

    /**
     * @return true if there are changes to the ui which have not been written to the beacon
     */
    private boolean tabChangesPending() {
        if (slotsAdapter != null) {
            boolean changesPending = false;
            for (int i = 0; i < slotsAdapter.getCount(); i++) {
                changesPending = changesPending || slotsAdapter.getItem(i).changesPending();
            }
            return changesPending;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gattClient.disconnect();
    }

    /**
     * ConfigurationListener
     * */
    @Override
    public void txPowerChanged(final int slot, final int txPower) {
        try {
            if (capabilities.isVariableTxPowerSupported()) {
                gattClient.writeActiveSlot(slot);
            }
            gattClient.writeRadioTxPower(txPower);
            int newlyReadTxPower = gattClient.readRadioTxPower()[0];
            if (txPower == newlyReadTxPower) {
                Bundle newTxPowerBundle = new Bundle();
                newTxPowerBundle.putString(Constants.TX_POWER, Integer.toString(newlyReadTxPower));
                slotsAdapter.getItem(slot + 1).updateInformation(newTxPowerBundle);
                Log.d(TAG, "Radio Tx Power changed to " + newlyReadTxPower);
            }
        } catch (GattClientException e) {
            Log.d(TAG, "Gatt Client Error when writing radio tx power to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing tx power to beacon");
        } catch (GattOperationException e) {
            Log.d(TAG, "Gatt Operation Error when writing tx power to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing tx power to beacon");
        }
    }

    /**
     * ConfigurationListener
     * */
    @Override
    public void advTxPowerChanged(final int slot, final int advTxPower) {
        try {
            if (capabilities.isVariableTxPowerSupported()) {
                gattClient.writeActiveSlot(slot);
            }
            gattClient.writeAdvertisedTxPower(advTxPower);
            int newlyReadAdvTx = gattClient.readAdvertisedTxPower()[0];
            if (advTxPower == newlyReadAdvTx) {
                Bundle newAdvTxBundle = new Bundle();
                newAdvTxBundle.putString(Constants.ADV_POWER, Integer.toString(newlyReadAdvTx));
                slotsAdapter.getItem(slot + 1).updateInformation(newAdvTxBundle);
                Log.d(TAG, "Advertised Tx Power changed to " + newlyReadAdvTx);
            }
        } catch (GattClientException e) {
            Log.d(TAG, "Gatt Client Error when adv tx power to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing adv tx power to beacon");
        } catch (GattOperationException e) {
            Log.d(TAG, "Gatt Operation Error when writing adv tx power to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing adv tx power to beacon");
        }
    }

    /**
     * ConfigurationListener
     * */
    @Override
    public void advIntervalChanged(final int slot, final int advInterval) {
        final BeaconTabFragment currentFragment = slotsAdapter.getItem(slot + 1);
        try {
            if (capabilities.isVariableAdvSupported()) {
                gattClient.writeActiveSlot(slot);
            }
            gattClient.writeAdvertisingInterval(advInterval);
            int newlyReadAdvInt = Utils.toInt(gattClient.readAdvertisingInterval());
            if (advInterval == newlyReadAdvInt) {
                Bundle newAdvIntBundle = new Bundle();
                newAdvIntBundle.putString(Constants.ADV_INTERVAL,
                        Integer.toString(newlyReadAdvInt));
                currentFragment.updateInformation(newAdvIntBundle);
                Log.d(TAG, "Advertising interval changed to " + advInterval);
            }
        } catch (GattClientException e) {
            Log.d(TAG, "Gatt Client Exception when writing adv interval to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing adv interval to beacon");
        } catch (GattOperationException e) {
            Log.d(TAG, "Gatt Operation Error when writing adv interval to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing adv interval to beacon");
        }
    }

    /**
     * ConfigurationListener
     * */
    @Override
    public void slotDataChanged(final int slot, final byte[] newSlotData) {
        try {
            Log.d(TAG, "New slot data: " + Arrays.toString(newSlotData));
            gattClient.writeActiveSlot(slot);
            gattClient.writeAdvSlotData(newSlotData);
            byte[] newlyReadSlotData = gattClient.readAdvSlotData();
            Bundle newSlotDataBundle = new Bundle();
            changeTabName(slot + 1, newlyReadSlotData);
            newSlotDataBundle.putByteArray(Constants.SLOT_DATA, newlyReadSlotData);
            slotsAdapter.getItem(slot + 1).updateInformation(newSlotDataBundle);
        } catch (GattClientException e) {
            Log.d(TAG, "Gatt Client Error when writing slot data to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing slot data to beacon");
        } catch (GattOperationException e) {
            Log.d(TAG, "Gatt Operation Error when writing slot data to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing slot data to beacon");
        }
    }

    public void saveCurrentChanges() {
        this.saveChangesAcrossAllTabs();
    }

    private void changeTabName(final int tabNo, final byte[] slotData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TabLayout.Tab tab = ((TabLayout) findViewById(R.id.tabs)).getTabAt(tabNo);
                if (Utils.slotIsEmpty(slotData)) {
                    tab.setText("--");
                } else {
                    tab.setText(Utils.getStringFromFrameType(slotData[0]));
                }
            }
        });
    }

    /**
     * ConfigurationListener
     * */
    @Override
    public void lockCodeChanged(final String newLockCode) {
        byte[] newCodeBytes = Utils.toByteArray(newLockCode);
        byte[] encryptedCode = Utils.aes128Encrypt(newCodeBytes, Utils.toByteArray(unlockCode));

        byte[] newLockCodeBytes = new byte[17];
        newLockCodeBytes[0] = 0;
        newLockCodeBytes = Utils.rewriteBytes(newLockCodeBytes, 1, 16, encryptedCode);
        try {
            gattClient.writeLockState(newLockCodeBytes);
            gattClient.unlock(newCodeBytes);
            UiUtils.showToast(BeaconConfigActivity.this, "Lock code changed successfully");
        } catch (GattClientException e) {
            Log.d(TAG, "Gatt Client Error when changing lock code of beacon", e);
            displayConnectionFailScreen("Something went wrong when changing lock code of beacon");
        } catch (GattOperationException e) {
            Log.d(TAG, "Gatt Operation Error when changing lock code of beacon", e);
            displayConnectionFailScreen("Something went wrong when changing lock code of beacon");
        }
    }

    /**
     * ConfigurationListener
     * */
    @Override
    public void factoryResetCalled() {
        disableDisplay();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] factoryResetCode = Utils.toByteArray("0b");
                    gattClient.writeFactoryReset(factoryResetCode);
                    setupBeaconInformationDisplay();
                    // setupBeaconInformationDisplay() will enable display after execution
                    UiUtils.showToast(BeaconConfigActivity.this, "Beacon was reset successfully");
                } catch (GattClientException e) {
                    Log.d(TAG, "Gatt Client Error when factory resetting beacon", e);
                    displayConnectionFailScreen("Something went wrong when resetting beacon");
                } catch (GattOperationException e) {
                    Log.d(TAG, "Gatt Operation Error when factory resetting beacon", e);
                    displayConnectionFailScreen("Something went wrong when resetting beacon");
                }
            }
        });

    }

    /**
     * ConfigurationListener
     * */
    @Override
    public void remainConnectableChanged(final boolean remainConnectable) {
        try {
            byte[] remainConnectableBytes = new byte[1];
            remainConnectableBytes[0] = (byte) (remainConnectable ? 1 : 0);
            gattClient.writeRemainConnectable(remainConnectableBytes);
        } catch (GattClientException e) {
            Log.d(TAG, "Gatt Client Error when writing remain connectable to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing remain connectable "
                    + "to beacon");
        } catch (GattOperationException e) {
            Log.d(TAG, "Gatt Operation Error when writing remain connectable to beacon", e);
            displayConnectionFailScreen("Something went wrong when writing remain connectable "
                    + "to beacon");
        }
    }
}