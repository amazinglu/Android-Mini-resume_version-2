package com.parabit.parabeacon.app.tech;

import android.content.Intent;
import android.os.Bundle;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.parabit.parabeacon.app.tech.auth.AuthManager;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /**
         * the session is time-out, need to log in again
         * */
        if (!getAuthManager().isSessionValid()/*!getCurrentState().isPersistentLogin()*/) {
            startLoginActivity();
            finish();
            return;
        }

        /**
         * authentication process:
         * try if we can anthenicate using the info in userPool
         * */
        getAuthManager().authenticate(new AuthManager.UserAuthHandler() {

            @Override
            public void onUserAuthenticationSuccess() {
                startScanningActivity();
                finish();
            }

            @Override
            public void onUserAuthenticationFailed(Exception exception) {
                startLoginActivity();
                finish();
            }

            @Override
            public void handleNewPasswordRequired(ChallengeContinuation continuation) {
                startNewPasswordActivity();
                finish();
            }

            @Override
            public String getUsername() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

        });

    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void startNewPasswordActivity() {
        Intent intent = new Intent(this, NewPasswordActivity.class);
        startActivity(intent);
    }

    private void startScanningActivity() {
        Intent intent = new Intent(this, ScanningActivity.class);
        startActivity(intent);
    }
}
