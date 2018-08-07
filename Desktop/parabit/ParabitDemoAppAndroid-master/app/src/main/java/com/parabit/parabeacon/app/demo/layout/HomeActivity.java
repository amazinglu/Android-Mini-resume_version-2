package com.parabit.parabeacon.app.demo.layout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.log.AppLogAppender;
import com.parabit.parabeacon.app.demo.log.AppLogListener;

import java.util.Observable;
import java.util.Observer;

public class HomeActivity extends BaseDemoActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private TextView mTextTitle;
    private TextView mTextSubtitle;

    private BottomSheetBehavior bottomSheetBehavior;

    /**
     * set up the bottom navigation view
     * */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            resetToolbar();

            switch (item.getItemId()) {
                case R.id.navigation_door:
                    mTextTitle.setText(R.string.title_door);
                    mTextSubtitle.setText(R.string.subtitle_door);
                    selectedFragment = DoorFragment.newInstance(new Bundle());
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, selectedFragment).commit();
                    return true;
                case R.id.navigation_find_atm_bank:
                    mTextTitle.setText(R.string.title_find_atm);
                    mTextSubtitle.setText(R.string.subtitle_find_atm);
                    selectedFragment = FindBankAtmFragment.newInstance(new Bundle());
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, selectedFragment).commit();
                    return true;
                case R.id.navigation_emergency:
                    mTextTitle.setText(R.string.title_emergency);
                    mTextSubtitle.setText(R.string.subtitle_emergency);
                    selectedFragment = EmergencyContactFragment.newInstance(new Bundle());
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, selectedFragment).commit();
                    return true;
//                case R.id.navigation_settings:
//                    mTextTitle.setText(R.string.title_app_settings);
//                    mTextSubtitle.setText(R.string.subtitle_app_settings);
//                    selectedFragment = SettingsFragment.newInstance(new Bundle());
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content, selectedFragment).commit();
//                    return true;
//                case R.id.navigation_about:
//                    mTextTitle.setText(R.string.title_about);
//                    mTextSubtitle.setText(R.string.subtitle_about);
//                    selectedFragment = AboutFragment.newInstance(new Bundle());
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content, selectedFragment).commit();
//                    return true;
            }
            return false;
        }

    };

    /**
     * what is the Observer doing
     * doing no thing
     * */
    private Observer mStateObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            refreshDoorView();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        refreshDoorView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * check out the permission of the app
         * */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
        }

        /**
         * set up the navigation drawer
         * */
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //setup menu
        boolean showAdvMenu = true;
        navigationView.getMenu().findItem(R.id.menu_advanced).setVisible(showAdvMenu);

        mTextTitle = (TextView) findViewById(R.id.fragment_title);
        mTextSubtitle = (TextView) findViewById(R.id.fragment_subtitle);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setSelectedItemId(R.id.navigation_door);

        setupLogView();

        mTextTitle.requestFocus();
        getCurrentState().addObserver(mStateObserver);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content);
            if (currentFragment !=null && "detailFragment".equals(currentFragment.getTag())) {
                onBackPressed();
                return true;
            }

            if (toggle.onOptionsItemSelected(item)){
                return true;
            }

            return true;
        }

        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return false;
        }

        if (id == R.id.nav_about) {
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

        if (id == R.id.nav_diagnostics) {
            showDiagnosticActivity();
            drawer.closeDrawers();
            return false;
        }

        if (id == R.id.nav_settings) {
            showSettingsActivity();
            drawer.closeDrawers();
            return false;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getCurrentState().deleteObserver(mStateObserver);
    }

    private void refreshDoorView() {
//        boolean autoUnlock = getIntent().getBooleanExtra("remote_unlock", false);
//        getIntent().putExtra("remote_unlock", false);
//        Door door = getCurrentState().getAvailableDoor();
//        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content);
//        if (currentFragment instanceof DoorFragment) {
//            ((DoorFragment)currentFragment).handleEnterDoorRegion(door, autoUnlock);
//        }
    }

    private void setupLogView() {
        View bottomSheet = findViewById(R.id.bottomSheetLayout);
        TextView logText = (TextView) bottomSheet.findViewById(R.id.txt_app_log);
        logText.setMovementMethod(new ScrollingMovementMethod());

        AppLogAppender.getInstance().addLogListener(new AppLogListener() {
            @Override
            public void onLog(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logText.append("\n");
                        logText.append(message);
                    }
                });

            }
        });

        log().info("listener attached");

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    //      bottomSheetHeading.setText(getString(R.string.text_collapse_me));
                } else {
                    //     bottomSheetHeading.setText(getString(R.string.text_expand_me));
                }

                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.e("Bottom Sheet Behaviour", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.e("Bottom Sheet Behaviour", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.e("Bottom Sheet Behaviour", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.e("Bottom Sheet Behaviour", "STATE_HIDDEN");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.e("Bottom Sheet Behaviour", "STATE_SETTLING");
                        break;
                }
            }


            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }
        });
    }

    private void resetToolbar() {
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void showDiagnosticActivity() {
        Intent diagnosticIntent =
                new Intent(this, DiagnosticActivity.class);

        startActivity(diagnosticIntent);
    }

    private void showSettingsActivity() {
        Intent diagnosticIntent =
                new Intent(this, SettingsActivity.class);

        startActivity(diagnosticIntent);
    }

}
