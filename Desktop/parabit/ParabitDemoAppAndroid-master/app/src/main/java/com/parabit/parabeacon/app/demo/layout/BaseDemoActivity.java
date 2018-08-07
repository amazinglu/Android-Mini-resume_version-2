package com.parabit.parabeacon.app.demo.layout;

/**
 * Created by williamsnyder on 8/29/17.
 */

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.parabit.parabeacon.app.demo.MMRDemoApplication;
import com.parabit.parabeacon.app.demo.manager.AppLogManager;
import com.parabit.parabeacon.app.demo.state.AppState;
import com.parabit.parabeacon.app.demo.state.AppStateManager;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManager;

import org.altbeacon.beacon.BeaconManager;
import org.slf4j.Logger;


/**
 * chceck out the bluetooth connect
 * hockeyapp update
 * */
public class BaseDemoActivity extends AppCompatActivity {

    private AppState currentState;

    public AppStateManager getAppStateManager() {
        if (getApplication() instanceof MMRDemoApplication) {
            return ((MMRDemoApplication) getApplication()).getAppStateManager();
        }
        return null;
    }

    public AppState getCurrentState() {
        if (currentState == null) {
            currentState = getAppStateManager().currentState();
        }
        return currentState;
    }

    public void loadAppState() {
        getAppStateManager().load();
    }

    public void saveAppState() {
        getAppStateManager().update(getCurrentState());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // make sure the bluetooth is open
        // TODO: learn about it later 1
        verifyBluetooth();
        // set update to client via hockey app
        // TODO: learn about it later 2
        registerForUpdates();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyBluetooth();
        //loadAppState();
        Tracking.startUsage(this);
        CrashManager.register(this);
        MMRDemoApplication.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //saveAppState();
        Tracking.stopUsage(this);
        unregisterForUpdates();
        MMRDemoApplication.activityPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterForUpdates();
    }

    protected Logger log() {
        return AppLogManager.getLogger();
    }

    public void showMessage(String title, String message) {
        new android.support.v7.app.AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    public void showMessage(String title, String message, DialogInterface.OnClickListener listener) {
        new android.support.v7.app.AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", listener).show();
    }

    public void showPopupMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(),
                        message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 80);
                toast.show();
            }
        });
    }

    private void registerForUpdates() {
        UpdateManager.register(this);
    }

    private void unregisterForUpdates() {
        UpdateManager.unregister();
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {

                BaseDemoActivity.this.setBluetooth(true);

//                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Turn on Bluetooth?");
//                builder.setMessage("The Parabit Beacon Demo requires Bluetooth be enabled in order to function.");
//                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        BaseDemoActivity.this.setBluetooth(true);
//                    }
//                });
//                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
//                        System.exit(0);
//                    }
//                });
//
//                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

    }

    private boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }
}
