package com.parabit.parabeacon.app.tech;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.parabit.parabeacon.app.tech.auth.AuthManager;
import com.parabit.parabeacon.app.tech.state.AppState;
import com.parabit.parabeacon.app.tech.state.AppStateManager;
import com.parabit.parabeacon.app.tech.utils.UiUtils;

/**
 * Created by williamsnyder on 11/27/17.
 */

public class BaseFragment extends Fragment {

    public AuthManager getAuthManager() {
        return ((MainApplication)getActivity().getApplication()).getAuthManager();
    }

    public AppStateManager getAppStateManager() {
        if (getActivity().getApplication() instanceof MainApplication) {
            return ((MainApplication) getActivity().getApplication()).getAppStateManager();
        }
        return null;
    }

    public AppState getCurrentState() {
        return getAppStateManager().currentState();
    }

    public void showPopupMessage(String message) {
        UiUtils.showToast(getActivity(), message);
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

}
