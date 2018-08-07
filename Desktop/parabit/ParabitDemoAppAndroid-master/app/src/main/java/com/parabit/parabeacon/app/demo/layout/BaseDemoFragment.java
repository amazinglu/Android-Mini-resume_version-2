package com.parabit.parabeacon.app.demo.layout;

import android.app.Application;
import android.support.v4.app.Fragment;

import com.parabit.parabeacon.app.demo.manager.AppLogManager;
import com.parabit.parabeacon.app.demo.state.AppState;

import org.slf4j.Logger;

/**
 * Created by williamsnyder on 8/29/17.
 */

public class BaseDemoFragment  extends Fragment {

    public AppState getCurrentState() {
        if (getActivity() instanceof BaseDemoActivity) {
            BaseDemoActivity activity = (BaseDemoActivity) getActivity();
            return activity.getCurrentState();
        }
        return null;
    }

    public void saveAppState() {
        if (getActivity() instanceof BaseDemoActivity) {
            BaseDemoActivity activity = (BaseDemoActivity) getActivity();
            activity.saveAppState();
        }
    }

    public Application getApplication() {
        return getActivity().getApplication();
    }

    protected Logger log() {
        return AppLogManager.getLogger();
    }
}
