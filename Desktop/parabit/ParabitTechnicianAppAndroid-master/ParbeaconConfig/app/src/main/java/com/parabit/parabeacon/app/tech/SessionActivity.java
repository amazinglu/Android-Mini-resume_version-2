package com.parabit.parabeacon.app.tech;

import android.content.DialogInterface;
import android.content.Intent;

import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.Events;

/**
 * Created by williamsnyder on 12/4/17.
 */

public class SessionActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (!getAuthManager().isSessionValid()) {
            ApplicationLogger.logDebug(Events.SESSION_TIMEOUT);
            showMessage("Session timeout",
                    "You have been logged out due to inactivity.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showLoginScreen();
                        }
                    });

        } else {
            getCurrentState().startSession();
        }
    }

    private void showLoginScreen(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
