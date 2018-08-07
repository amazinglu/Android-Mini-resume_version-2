package com.parabit.parabeacon.app.demo.auth.layout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.auth.R;
import com.parabit.parabeacon.app.demo.auth.AuthManager;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewPasswordFragment extends BaseFragment {

    private String password;
    private TextInputEditText mTextPassword;
    private TextInputEditText mTextPasswordConfirm;

    public NewPasswordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_new_password, container, false);
        setupFragment(fragment);
        return fragment;
    }


    public void submitPassword() {
        String newPassword = getPassword();
        String confirmPassword = getConfirmPassword();

        try {

            if (!newPassword.equals(confirmPassword)) {
                throw new Exception("Passwords much match.");
            }

            getAuthManager().setNewPassword(newPassword);
        } catch (Exception e) {
            handleException(e);
        }
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

    private void setupFragment(View fragment) {
        mTextPassword = (TextInputEditText) fragment.findViewById(R.id.txt_new_password);
        mTextPasswordConfirm = (TextInputEditText) fragment.findViewById(R.id.txt_new_password_confirm);

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

        getAuthManager().setNewPasswordHandler(new AuthManager.NewPasswordHandler() {
            @Override
            public void onNewPasswordSuccess() {
                getCurrentState().startSession();
                launchMainActivity();
            }

            @Override
            public void onNewPasswordFailure(Exception exception) {
                String message = exception.getMessage();
                if (exception instanceof com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException) {
                    message = ((com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException)exception).getErrorMessage();
                    showMessage(getString(R.string.login_error_title), message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           getActivity().finish();
                        }
                    });
                    return;
                }
                if (exception instanceof com.amazonaws.services.cognitoidentityprovider.model.InvalidPasswordException) {
                    message = ((com.amazonaws.services.cognitoidentityprovider.model.InvalidPasswordException)exception).getErrorMessage();
                }
                showMessage(getString(R.string.login_error_title),message);
            }
        });
    }

}