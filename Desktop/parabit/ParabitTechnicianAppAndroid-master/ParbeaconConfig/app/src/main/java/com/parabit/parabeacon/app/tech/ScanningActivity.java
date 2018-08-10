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

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parabit.beacon.ParabitBeaconManager;
import com.parabit.beacon.dfu.DfuFileHelper;
import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;
import com.parabit.parabeacon.app.tech.utils.UiUtils;

import org.zakariya.stickyheaders.StickyHeaderLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the main activity in Beaconfig. It asks for location permissions, turns on bluetooth and
 * initialises the BLE Scanner. Then it scans for nearby beacons and displays the results in a list.
 *
 * Each entry in the list represents a unique beacon with information from several scan results
 * whose information is saved in a BeaconScanData object.
 *
 * On click of any of the list entries, a new activity starts - BeaconConfigActivity. It connects
 * to the beacon and allows per slot configuration of the beacon.
 */
public class ScanningActivity extends SessionActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private static final int PERMISSION_COARSE_LOCATION = 2;
    private static final int WRITE_EXTERNAL_STORAGE = 3;
    private static final String TAG = ScanningActivity.class.getSimpleName();

    private DrawerLayout drawer;
    private BeaconListAdapter2 beaconsListAdapter;
    private BluetoothAdapter btAdapter;
    private BeaconScanner scanner;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        new SavedConfigurationsManager(this).initialiseConfigurationSaving();
        executor = Executors.newSingleThreadExecutor();

        /**
         * recycler view for the beacons
         * */
        beaconsListAdapter
                = new BeaconListAdapter2(new ArrayList<BeaconScanData>(), getApplication());
        RecyclerView beaconsRecyclerView = (RecyclerView) findViewById(R.id.rv);
        beaconsRecyclerView.setAdapter(beaconsListAdapter);
        beaconsRecyclerView.setLayoutManager(new StickyHeaderLayoutManager());
        //beaconsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        final FloatingActionButton refresh = (FloatingActionButton) findViewById(R.id.fab);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });

        setUpThrobber();
        getRequiredPermissions();

        beaconsRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return swipeRefreshLayout.isRefreshing();
            }
        });

        beaconsRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                Log.d(TAG, "On interception touch listener " + swipeRefreshLayout.isRefreshing());
                return swipeRefreshLayout.isRefreshing();
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        String token = getAuthManager().getBeaconToken();
        String url = getAuthManager().getBeaconURL();
        String username = getAuthManager().getUsername();
        String appId = getAuthManager().getAppId();

        try {
            ParabitBeaconManager beaconManager = ParabitBeaconManager.getInstance(appId, url, token);
            beaconsListAdapter.setParabitBeaconManager(beaconManager);
        } catch (Exception e) {
            handleBeaconAPIUnavailable(e);
        }

        View headerView = navigationView.getHeaderView(0).findViewById(R.id.nav_header_container);
        ((TextView)headerView.findViewById(R.id.nav_header_title)).setText("Currently logged in as:");
        ((TextView)headerView.findViewById(R.id.nav_header_subtitle)).setText(username);

    }

    @Override
    protected void onResume() {
        super.onResume();
        scan();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            getAuthManager().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return false;
        }

        if (id == R.id.nav_help) {
            ApplicationLogger.logEvent(Events.MENU_HELP);
            showMessage(getString(R.string.not_implemented_title), getString(R.string.not_implemented));
            return false;
        }

        if (id == R.id.nav_about) {
            ApplicationLogger.logEvent(Events.MENU_ABOUT);
            PackageManager manager = getPackageManager();
            PackageInfo info = null;
            try {
                info = manager.getPackageInfo(getPackageName(), 0);
                String version = info.versionName;
                showMessage("About", getString(R.string.app_name) + "\n\n" + version);
            } catch (PackageManager.NameNotFoundException e) {
                //ignore
            }
            return false;
        }

        if (id == R.id.nav_feedback) {
            showFeedbackActivity();
            drawer.closeDrawers();
            return false;
        }

        if (id == R.id.nav_report_issue) {
            showProblemActivity();
            drawer.closeDrawers();
            return false;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleBeaconAPIUnavailable(Exception e) {
        ApplicationLogger.logError(Events.API_FAILURE,e);
        showPopupMessage("Unable to access Beacon service.");
    }

    private void showFeedbackActivity(){
        ApplicationLogger.logEvent(Events.MENU_FEEDBACK);

        Intent feedbackIntent =
                new Intent(this, FeedbackActivity.class);
        feedbackIntent.putExtra("category", 0);

        startActivity(feedbackIntent);
    }

    private void showProblemActivity(){
        ApplicationLogger.logEvent(Events.MENU_REPORT_PROBLEM);

        Intent feedbackIntent =
                new Intent(this, FeedbackActivity.class);
        feedbackIntent.putExtra("category", 1);

        startActivity(feedbackIntent);
    }

    private void showFeedbackForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Comment");

        final EditText input = new EditText(this);

        input.setText("test");
        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setSingleLine(false);
        input.setLines(5);
        input.setMaxLines(5);
        input.setGravity(Gravity.LEFT | Gravity.TOP);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


            }
        });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * scan process:
     *
     * Attempts to scan for beacons. First checks whether bluetooth is turned on and deals with
     * the case when it is not. During this, the screen is disabled and the swipeRefreshLayout is
     * refreshing.
     */
    private void scan() {
        Log.d(TAG, "Scanning...");
        if (btAdapter == null || !btAdapter.isEnabled()) {
            requestBluetoothOn();
        } else {
            /**
             * trigger the swipefreshLayout programming
             * */
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            findViewById(R.id.grey_out_slot).setVisibility(View.VISIBLE);
            UiUtils.showToast(this, "Scanning...");
            ApplicationLogger.logEvent(Events.BEACON_SCAN);

            /**
             * the executor will execute the Runnable
             * */
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    scanner.scan();
                }
            });
        }
    }

    /**
     * Callback for when the scan has finished. This enables the screen again and informs the
     * RecyclerViewAdapter that new data is available to be displayed.
     *
     * @param scanDataList the information gathered about each beacon over the whole time of the
     *                     scan.
     */
    public void scanComplete(final List<BeaconScanData> scanDataList) {
        Log.d(TAG, "Scanning complete.");
        Collections.sort(scanDataList, new Comparator<BeaconScanData>() {
            @Override
            public int compare(BeaconScanData b1, BeaconScanData b2) {
                return (b2.rssi - b1.rssi);
            }
        });


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /**
                 * update the data of the recycler view
                 * */
                beaconsListAdapter.setData(scanDataList);
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        findViewById(R.id.grey_out_slot).setVisibility(View.GONE);
                        String message = scanDataList.size() + " results were found";
                        UiUtils.showToast(ScanningActivity.this,message);
                    }
                });
            }
        });
    }

    private void setupScanner() {
        Log.d(TAG, "Setting up scanner...");
        BluetoothManager manager = (BluetoothManager) getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = manager.getAdapter();

        requestBluetoothOn();
    }

    private void requestBluetoothOn() {
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth not enabled, requesting permission.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BLUETOOTH);
        } else if (scanner == null) {
            scanner = new BeaconScanner(this, btAdapter);
            scan();
        }
    }

    private void getRequiredPermissions() {
        Log.d(TAG, "Getting Permissions...");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_COARSE_LOCATION);
        } else {
            setupScanner();
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            DfuFileHelper.createSamples(this);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Bluetooth enable permission granted.");
                if (btAdapter == null) {
                    setupScanner();
                }
                scanner = new BeaconScanner(this, btAdapter);
                scan();
            } else {
                Log.d(TAG, "Bluetooth enable permission denied. Closing...");
                showFinishingAlertDialog("Bluetooth is required",
                        "App will close since the permission was denied");
            }
        }

        if (requestCode == WRITE_EXTERNAL_STORAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Storage enable permission granted.");
                DfuFileHelper.createSamples(this);
            } else {
                Log.d(TAG, "Storage enable permission denied.");
                //showFinishingAlertDialog("Bluetooth is required",
                        //"App will close since the permission was denied");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String permissions[], int[] grantResults) {
        switch (code) {
            case PERMISSION_COARSE_LOCATION:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupScanner();
                    Log.d(TAG, "PERMISSION_REQUEST_COARSE_LOCATION granted");
                } else {
                    showFinishingAlertDialog("Coarse location access is required",
                            "App will close since the permission was denied");
                }
        }
    }

    private void showFinishingAlertDialog(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
    }

    private void setUpThrobber() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.throbber);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scan();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }
}
