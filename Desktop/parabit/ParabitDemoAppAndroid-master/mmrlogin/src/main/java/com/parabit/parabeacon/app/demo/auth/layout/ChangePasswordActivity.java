package com.parabit.parabeacon.app.demo.auth.layout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.auth.AuthManager;
import com.parabit.parabeacon.app.demo.auth.R;

public class ChangePasswordActivity extends BaseActivity implements AuthManager.ResetPasswordHandler {

    private TextView mTextHeader;
    private TextView mTextSubheader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Reset Password");

        mTextHeader = (TextView) findViewById(R.id.txt_change_password_header);
        mTextSubheader = (TextView) findViewById(R.id.txt_change_password_subheader);

        showUsernameSelection();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.change_password_container);
            if (f instanceof ChangePasswordUsernameFragment) {
                finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
            return true;
        } else if (i == R.id.action_set_new_password) {
            handleNextStep();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTitle(String title, String subtitle) {
        mTextHeader.setText(title);
        mTextSubheader.setText(subtitle);

        if (title == null || title.isEmpty()) {
            mTextHeader.setVisibility(View.GONE);
        } else {
            mTextHeader.setVisibility(View.VISIBLE);
        }

        if (subtitle == null || subtitle.isEmpty()) {
            mTextSubheader.setVisibility(View.GONE);
        } else {
            mTextSubheader.setVisibility(View.VISIBLE);
        }
    }

    private void showUsernameSelection() {
        Fragment changePasswordUsernameFragment = new ChangePasswordUsernameFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.change_password_container, changePasswordUsernameFragment).commit();
        setTitle("PLEASE ENTER YOUR EMAIL ADDRESS","We’ll email you a verification code to confirm your identity.");
    }

    private void showCodeInput() {
        Fragment changePasswordVerificationFragment = new ChangePasswordVerificationFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.change_password_container, changePasswordVerificationFragment).commit();
        setTitle("PLEASE ENTER YOUR VERIFICATION CODE BELOW","Tip: you might need to check your Junk/Spam folder");
    }

    private void showPasswordInput() {
        Fragment changePasswordFragment = new ChangePasswordFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.change_password_container, changePasswordFragment).commit();
        setTitle("PLEASE ENTER YOUR NEW PASSWORD","");

    }

    public void handleNextStep() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.change_password_container);
        if (f instanceof ChangePasswordUsernameFragment) {
            String username = ((ChangePasswordUsernameFragment)f).getUsername();
            beginPasswordReset(username);
        }
        if (f instanceof ChangePasswordVerificationFragment) {
            String code = ((ChangePasswordVerificationFragment)f).getVerificationCode();
            handleCodeReceived(code);
        }
        if (f instanceof ChangePasswordFragment) {
            String password = null;
            try {
                ((ChangePasswordFragment)f).submitPassword();
            } catch (Exception e) {
               handleException(e);
            }
        }

    }

    private void handleCodeReceived(String code) {
        getAuthManager().setResetPassordVerification(code);
        showPasswordInput();
    }

    private void beginPasswordReset(String username) {
        getAuthManager().requestPasswordReset(username, this);
    }

    private void handleException(Exception e) {
        showMessage("Error", e.getMessage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_password, menu);
        return true;
    }


    @Override
    public void onCodeSent() {
        showMessage("Verification Code Sent", "Please check your email for a verification code.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showCodeInput();
            }
        });
    }

    @Override
    public void onPasswordChangeSuccess() {
        showMessage("Success", "Password changed successfully", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    @Override
    public void onPasswordChangeFailure(Exception e) {
        showMessage("Unable to reset password", e.getMessage());
    }
}
