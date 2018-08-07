package com.parabit.parabeacon.app.demo.auth.layout;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.parabit.parabeacon.app.demo.auth.AuthManager;
import com.parabit.parabeacon.app.demo.auth.state.AuthAppState;
import com.parabit.parabeacon.app.demo.auth.state.AuthAppStateManager;

/**
 * Created by williamsnyder on 11/27/17.
 */

public class BaseFragment extends Fragment {

    public AuthManager getAuthManager() {
        return AuthManager.getInstance(this.getActivity());
    }

    public AuthAppStateManager getAppStateManager() {
        return AuthAppStateManager.getInstance(this.getActivity());
    }

    public AuthAppState getCurrentState() {
        return getAppStateManager().currentState();
    }

    public void showMessage(String title, String message) {
        new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    public void showMessage(String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message)
                .setPositiveButton("OK", listener).show();
    }

    public void saveAppState() {
        getAppStateManager().update(getCurrentState());
    }

    public void launchMainActivity() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity)getActivity()).launchMainActivity();
        }
    }

}
