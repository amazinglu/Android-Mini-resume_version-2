package com.parabit.parabeacon.app.demo.auth.layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.services.cognitoidentityprovider.model.InvalidParameterException;
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.parabit.parabeacon.app.demo.auth.R;
import com.parabit.parabeacon.app.demo.auth.AuthManager;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {

    private String password = null;

    // UI references.
    private TextInputEditText mEmailView;
    private TextInputEditText mPasswordView;
    private TextView mForgotPassword;
    private TextView mTextLoginVersion;
    private View mProgressView;
    private View mLoginFormView;
//    private Switch mPersistLoginSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_login);

        // Set up the login form.
//        mPersistLoginSwitch = (Switch) findViewById(R.id.switch_remember_me);
//        mPersistLoginSwitch.setChecked(getCurrentState().isPersistentLogin());

        mEmailView = (TextInputEditText) findViewById(R.id.txt_username);

        if (getCurrentState().getUsername() != null) {
            mEmailView.setText(getCurrentState().getUsername());
        }

        mPasswordView = (TextInputEditText) findViewById(R.id.txt_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mForgotPassword = (TextView) findViewById(R.id.txt_forgot_password);
        mForgotPassword.setClickable(true);
        mForgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.btn_login);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mTextLoginVersion = (TextView) findViewById(R.id.txt_login_version);

        setupDisplayedVersion();

        getAppStateManager().currentState().endSession();
        getAuthManager().signOut();
    }

    private void setupDisplayedVersion() {
//        if (getApplication() instanceof MainApplication) {
//            String version = ((MainApplication)getApplication()).getAppVersion();
//            if (version == null) {
//                version = "Dev version";
//            }
//            mTextLoginVersion.setText(version);
//        }
    }

    private void handleForgotPassword() {
        Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

//        final boolean persist = mPersistLoginSwitch.isChecked();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            this.password = password;

            getAuthManager().authenticate(new AuthManager.UserAuthHandler() {
                @Override
                public void onUserAuthenticationSuccess() {
                    getCurrentState().setUsername(email);
//                    getCurrentState().setPersistentLogin(persist);

                    getCurrentState().startSession();
                    saveAppState();

                    launchMainActivity();
                }

                @Override
                public void onUserAuthenticationFailed(Exception exception) {
                    showProgress(false);
                    Log.d("Cognito", exception.getMessage());
                    String message = exception.getMessage();
                    if (exception instanceof NotAuthorizedException) {
                        message = ((NotAuthorizedException)exception).getErrorMessage();
                    }
                    if (exception instanceof UserNotFoundException) {
                        message = ((UserNotFoundException)exception).getErrorMessage();
                    }
                    if (exception instanceof InvalidParameterException) {
                        message = "Please enter a valid email address.";
                    }
                    showMessage(getString(R.string.login_error_title),message);
                }

                @Override
                public void handleNewPasswordRequired(ChallengeContinuation continuation) {
                    if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                        getCurrentState().setUsername(email);
//                        getCurrentState().setPersistentLogin(persist);
                        saveAppState();

                        showProgress(false);
                        getAuthManager().setNewPasswordContinuation((NewPasswordContinuation) continuation);
                        showNewPasswordActivity();
                    }
                }

                @Override
                public String getUsername() {
                    return email;
                }

                @Override
                public String getPassword() {
                    return password;
                }
            });

        }
    }

    private void showNewPasswordActivity() {
        Intent newPasswordActivity = new Intent(LoginActivity.this, NewPasswordActivity.class);
        startActivity(newPasswordActivity);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        if (show == true) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mLoginFormView.getWindowToken(), 0);
        }

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


}

