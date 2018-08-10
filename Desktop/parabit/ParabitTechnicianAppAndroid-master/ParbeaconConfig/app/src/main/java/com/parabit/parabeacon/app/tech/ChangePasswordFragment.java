package com.parabit.parabeacon.app.tech;

import android.support.design.widget.TextInputEditText;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChangePasswordFragment extends BaseFragment {

    private String password;

    private TextInputEditText mTextPassword;
    private TextInputEditText mTextPasswordConfirm;

    public ChangePasswordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_change_password, container,
                false);
        setupFragment(fragment);
        return fragment;
    }

    /**
     * login process => handle forget password
     * after entering the bew password
     * */
    public void submitPassword() {
        String newPassword = getPassword();
        String confirmPassword = getConfirmPassword();

        try {

            if (!newPassword.equals(confirmPassword)) {
                throw new Exception("Passwords much match.");
            }
            ApplicationLogger.logError(ParabitLogConstants.Events.PWD_RESET_SET);
            getAuthManager().submitResetPassword(newPassword);
        } catch (Exception e) {
            ApplicationLogger.logError(ParabitLogConstants.Events.PWD_RESET_FAILED,e);
            handleException(e);
        }
    }

    private void setupFragment(View fragment) {
        mTextPassword = (TextInputEditText) fragment.findViewById(R.id.txt_change_password);
        mTextPasswordConfirm = (TextInputEditText) fragment.findViewById(R.id.txt_change_password_confirm);

        mTextPasswordConfirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    submitPassword();
                    return true;
                }
                return false;
            }
        });

    }

    private String getPassword() {
        return mTextPassword.getText().toString();
    }

    private String getConfirmPassword() {
        return mTextPasswordConfirm.getText().toString();
    }

    private void handleException(Exception e) {
        showMessage("Error", e.getMessage());
    }
}
