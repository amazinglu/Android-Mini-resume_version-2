package com.parabit.mmrbt;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.cheergoivan.totp.TOTPAuthenticator;
import com.parabit.mmrbt.api.AuthenticationRequest;
import com.parabit.mmrbt.api.AuthenticationResponse;
import com.parabit.mmrbt.api.BankLocation;
import com.parabit.mmrbt.api.BankLocationHandler;
import com.parabit.mmrbt.api.BankLocationManager;
import com.parabit.mmrbt.api.BeaconInfo;
import com.parabit.mmrbt.api.BeaconInfoHandler;
import com.parabit.mmrbt.api.DeviceRegistration;
import com.parabit.mmrbt.api.DeviceRegistrationResult;
import com.parabit.mmrbt.api.LocationHandler;
import com.parabit.mmrbt.api.RegistrationHandler;
import com.parabit.mmrbt.api.SecureStorage;
import com.parabit.mmrbt.api.UnlockCommand;
import com.parabit.mmrbt.api.UnlockCommandResult;
import com.parabit.mmrbt.api.UnlockHandler;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by williamsnyder on 2/21/18.
 */

public class ParabitBeaconSDK {

    private static final String TAG = "ParabitBeaconSDK";

    private static ParabitBeaconSDK instance;

    private Context context;
    private SecureStorage secureStorage;
    private ParabitDoorManager doorManager;
    private TOTPAuthenticator totpAuthenticator;
    private BeaconMonitor mBeaconMonitor;
    private ParabitBeaconManager mParabitBeaconService;
    private BankLocationManager mBankLocationManager;
    private ParabitAuthenticationManager mAuthManager;

    private static final String PARABIT_ID_TOKEN = "parabit.id.token";
    private static final String PARABIT_ID = "parabit.id";

    private String controlURL;
    private String controlKey;

    private String beaconURL;
    private String beaconKey;

    private String locationURL;
    private String locationKey;

    private boolean isDebugMode;

    private RegistrationHandler _registrationHandler;

    private  ParabitBeaconSDK(Context context)  {
        this.context = context;

        /**
         * authentication manager
         * */
        String authUrl = context.getString(R.string.auth_api_url);
        String authKey = context.getString(R.string.auth_api_key);
        mAuthManager = ParabitAuthenticationManager.getInstance(authUrl, authKey);

        /**
         * Beacon Monitor
         * */
        mBeaconMonitor = BeaconMonitor.getInstanceForApplication(context.getApplicationContext());

        totpAuthenticator = TOTPAuthenticator.builder().build();
    }

    public static void register(Context context, RegistrationHandler registrationHandler) {

        if (instance == null) {
            instance = new ParabitBeaconSDK(context);
        }

        instance.doAuthenticateThenRegister(registrationHandler);
    }

    public static void unlock(String serialNumber, int duration, UnlockHandler unlockHandler) {
        // TODO: why we need to check the register here
        instance.registerIfNecessary(new RegistrationHandler() {
            @Override
            public void onRegistered() {
                instance.doUnlock(serialNumber, duration, unlockHandler);
            }

            @Override
            public void onError(String s) {
                unlockHandler.onError("App not registered.");
            }
        });

    }

    public static void getBeaconInfo(int serialNumber, BeaconInfoHandler beaconInfoHandler) {
        instance.registerIfNecessary(new RegistrationHandler() {
            @Override
            public void onRegistered() {
                instance.doGetInfo(serialNumber, beaconInfoHandler);
            }

            @Override
            public void onError(String s) {
                beaconInfoHandler.onError("App not registered.");
            }
        });
    }

    public static void getBankLocation(String locationId, BankLocationHandler bankLocationHandler) {
        instance.registerIfNecessary(new RegistrationHandler() {
            @Override
            public void onRegistered() {
                instance.doGetBankLocation(locationId, bankLocationHandler);
            }

            @Override
            public void onError(String s) {
                bankLocationHandler.onError("App not registered.");
            }
        });
    }

    public static Set<Integer> beaconsInRange() {
        return instance.mBeaconMonitor.getBeaconsInRange();
    }

    public static void addBeaconListener(BeaconListener beaconListener) {
        instance.mBeaconMonitor.addBeaconNotifier(beaconListener);
    }

    public static void removeBeaconListener(BeaconListener beaconListener) {
        instance.mBeaconMonitor.removeBeaconNotifier(beaconListener);
    }

    public static void getNearbyLocations(double lat, double lon, LocationHandler locationHandler) {
        instance.registerIfNecessary(new RegistrationHandler() {
            @Override
            public void onRegistered() {
                instance.doGetNearbyLocations(lat, lon, locationHandler);
            }

            @Override
            public void onError(String s) {
                locationHandler.onError("App not registered.");
            }
        });
    }

    public static void setDebugMode(boolean debugEnabled) {
        instance.isDebugMode = debugEnabled;
    }

    private SecureStorage getSecureStorage() {
        if (secureStorage != null) {
            return secureStorage;
        }

        try {
            secureStorage = SecureStorage.getInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
            //figure out what do to with this
        }

        return secureStorage;
    }

    private void registerIfNecessary(RegistrationHandler registrationHandler) {
        doRegister(new RegistrationHandler() {
            @Override
            public void onRegistered() {
                ParabitBeaconSDK.this._registrationHandler.onRegistered();
                registrationHandler.onRegistered();
            }

            @Override
            public void onError(String s) {
                ParabitBeaconSDK.this._registrationHandler.onError(s);
                registrationHandler.onError(s);
            }
        });
    }

    private boolean checkWriteSecurePermission() {
        return true;
//        String permission = Manifest.permission.WRITE_SECURE_SETTINGS;
//        int res = context.checkCallingOrSelfPermission(permission);
//        return (res == PackageManager.PERMISSION_GRANTED);
    }
    /**
     * Oauth login function
     * first authenticate then register and get the token
     * */
    private void doAuthenticateThenRegister(RegistrationHandler registrationHandler) {
        String appId = getApplicationId(context);

        this._registrationHandler = registrationHandler;

        /**
         * a object the contains the application ID
         * */
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setAppId(appId);

        /**
         * get authentication of parabit
         *
         * send a appID to service
         * return back the result
         * */
        mAuthManager.authenticate(authRequest, new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                AuthenticationResponse result = response.body();
                if (result != null) {

                    beaconKey = result.getBeaconKey();
                    beaconURL = result.getBeaconURL();
                    controlKey = result.getControlKey();
                    controlURL = result.getControlURL();
                    locationKey = result.getLocationKey();
                    locationURL = result.getLocationURL();

                    refreshEndpoints();
                    /**
                     * register the device
                     *
                     * send nothing to the server
                     * return Token and DeviceID
                     * */
                    doRegister(registrationHandler);
                } else {
                    registrationHandler.onError("Invalid appId.");
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                registrationHandler.onError(t.getLocalizedMessage());
            }
        });
    }

    private void doRegister(RegistrationHandler registrationHandler) {

        if (!checkWriteSecurePermission()) {
            registrationHandler.onError("No permissions for secure preferences.");
            return;
        }

        if (alreadyRegistered()) {
            registrationHandler.onRegistered();
            return;
        }

        if (doorManager == null) {
            return;
        }

        DeviceRegistration deviceRegistration = new DeviceRegistration();
        doorManager.register(deviceRegistration, new Callback<DeviceRegistrationResult>() {
            @Override
            public void onResponse(Call<DeviceRegistrationResult> call, Response<DeviceRegistrationResult> response) {
                DeviceRegistrationResult result = response.body();
                if (result != null) {
                    store(PARABIT_ID_TOKEN, result.getToken());
                    store(PARABIT_ID, result.getDeviceId());
                    registrationHandler.onRegistered();
                }
            }

            @Override
            public void onFailure(Call<DeviceRegistrationResult> call, Throwable t) {
                System.out.println(t.getLocalizedMessage());
                registrationHandler.onError(t.getLocalizedMessage());
            }
        });
    }

    /**
     * get the info of the door base on the serial_number of the beacon
     * */
    private void doGetInfo(int serialNumber, BeaconInfoHandler beaconInfoHandler) {
        if (getParabitBeaconManager() == null) {
            return;
        }

        getParabitBeaconManager().getBeaconBySerialNumber(serialNumber,
                new Callback<BeaconInfo>() {
                    @Override
                    public void onResponse(Call<BeaconInfo> call, Response<BeaconInfo> response) {
                        BeaconInfo beaconInfo = response.body();
                        beaconInfoHandler.onResult(beaconInfo);
                    }

                    @Override
                    public void onFailure(Call<BeaconInfo> call, Throwable t) {
                        beaconInfoHandler.onError(t.getMessage());
                    }
                });
    }

    /**
     * ask the server to unlock the door
     * */
    private void doUnlock(String serialNumber, int doorOpenTime, final UnlockHandler unlockHandler) {
        // get the secret from share preference
        /**
         * secret is the token that we can from register
         * */
        String secret = retrieve(PARABIT_ID_TOKEN);

        UnlockCommand unlockCommand = new UnlockCommand();
        /**
         * TOTPAuthenticator
         * a github project that generate time-base one-time password
         * */
        TOTPAuthenticator auth = TOTPAuthenticator.builder().build();

        /**
         * post body:
         * token, serialNumber, DeviceId, doorOpenTime
         *
         * seem this is only a fake door, the response is null at the moment
         * TODO: ask about the status of the unlock request
         *
         * the totpToken is generate by the PARABIT_ID_TOKEN
         * */
        String totpToken = totpAuthenticator.generateTOTP(secret.getBytes());
        unlockCommand.setToken(totpToken);
        unlockCommand.setSerialNumber(serialNumber);
        unlockCommand.setDeviceId(retrieve(PARABIT_ID));
        unlockCommand.setDoorOpenTime(doorOpenTime);

        doorManager.unlock(unlockCommand, new Callback<UnlockCommandResult>() {
            @Override
            public void onResponse(Call<UnlockCommandResult> call, Response<UnlockCommandResult> response) {
                UnlockCommandResult result = response.body();
                if (result != null) {
                    unlockHandler.onResult(result.isUnlocked());
                    return;
                }

                unlockHandler.onError("Unable to unlock the door");
            }

            @Override
            public void onFailure(Call<UnlockCommandResult> call, Throwable t) {
                unlockHandler.onError(t.getMessage());
            }
        });

    }

    private void doGetNearbyLocations(double lat, double lon, LocationHandler locationHandler) {
        if (getParabitBeaconManager() == null) {
            return;
        }

        getBankLocationManager().getNearbyLocations(lat, lon,
                new Callback<List<BankLocation>>() {
                    @Override
                    public void onResponse(Call<List<BankLocation>> call, Response<List<BankLocation>> response) {
                        List<BankLocation> locations = response.body();
                        locationHandler.onSuccess(locations);
                    }

                    @Override
                    public void onFailure(Call<List<BankLocation>> call, Throwable t) {
                        locationHandler.onError(t.getMessage());
                    }
                });
    }

    private void doGetBankLocation(String locationId, BankLocationHandler bankLocationHandler) {
        if (getParabitBeaconManager() == null) {
            return;
        }

        getBankLocationManager().getBankLocation(locationId,
                new Callback<BankLocation>() {
                    @Override
                    public void onResponse(Call<BankLocation> call, Response<BankLocation> response) {
                        BankLocation bankLocation = response.body();
                        bankLocationHandler.onResult(bankLocation);
                    }

                    @Override
                    public void onFailure(Call<BankLocation> call, Throwable t) {
                        bankLocationHandler.onError(t.getMessage());
                    }
                });
    }

    private ParabitBeaconManager getParabitBeaconManager() {
        return mParabitBeaconService;
    }

    private BankLocationManager getBankLocationManager() {
        return mBankLocationManager;
    }

    private void store(String key, String value) {
        try {
            getSecureStorage().putValue(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get the value from share preference with the key of "key"
     * */
    private String retrieve(String key) {
        try {
            return getSecureStorage().getValue(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean alreadyRegistered() {
        try {
            return getSecureStorage().getValue(PARABIT_ID) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void refreshEndpoints() {

        String appId = getApplicationId(context);

        doorManager = ParabitDoorManager.getInstance(controlURL, controlKey, appId);
        mBankLocationManager = BankLocationManager.getInstance(locationURL, locationKey, appId);
        mParabitBeaconService = ParabitBeaconManager.getInstance(beaconURL, beaconKey, appId);
    }

    /**
     * return the application ID
     * */
    private static String getApplicationId(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            /**
             * the matadata in Application on manifest
             * */
            Bundle bundle = ai.metaData;
            String myApiKey = bundle.getString("com.parabit.mmrbtsdk.appIdentifier");
            return myApiKey;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        return null;
    }

}
