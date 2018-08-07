package com.parabit.parabeacon.app.demo.layout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.log.AppLogAppender;
import com.parabit.parabeacon.app.demo.log.AppLogListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class BottomSheetFragment extends Fragment {


    public BottomSheetFragment() {
        // Required empty public constructor
    }

    public static BottomSheetFragment newInstance(Bundle bundle) {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.setArguments(bundle);
        return bottomSheetFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_bottom_sheet, container, false);

        TextView logText = (TextView) v.findViewById(R.id.txt_app_log);

        AppLogAppender.getInstance().addLogListener(new AppLogListener() {
            @Override
            public void onLog(String message) {
                logText.append(message);
            }
        });

        return v;
    }

}
