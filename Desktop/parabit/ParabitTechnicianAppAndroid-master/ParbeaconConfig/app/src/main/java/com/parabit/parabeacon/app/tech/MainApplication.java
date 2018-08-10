package com.parabit.parabeacon.app.tech;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.parabit.parabeacon.app.tech.auth.AuthManager;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;
import com.parabit.parabeacon.app.tech.state.AppState;
import com.parabit.parabeacon.app.tech.state.AppStateManager;
import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;


/**
 * Created by williamsnyder on 11/6/17.
 */

public class MainApplication extends Application {

    private AuthManager userUtils;
    private AppStateManager appStateManager;

    public void onCreate() {
        super.onCreate();

        /**
         * set up the app state
         * */
        appStateManager = new AppStateManager(getApplicationContext());
        AppState currentState = appStateManager.currentState();
        appStateManager.update(currentState);

        userUtils = AuthManager.getInstance(this.getApplicationContext(), appStateManager);

        ApplicationLogger.initialize(this);
        ApplicationLogger.setDebug(appStateManager.currentState().isDebugEnabled());

        ApplicationLogger.logEvent(Events.APP_LAUNCHED);
    }

    public AuthManager getAuthManager() {
        return userUtils;
    }

    public AppStateManager getAppStateManager() {
        return this.appStateManager;
    }

    public String getAppVersion() {
        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            //ignore
        }
        return null;
    }

}
