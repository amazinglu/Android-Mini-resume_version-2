package com.parabit.parabeacon.app.demo.auth.layout;


import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.parabit.parabeacon.app.demo.auth.R;

public class ChangePasswordUsernameFragment extends BaseFragment {

    private TextInputEditText mTextUsername;

    public ChangePasswordUsernameFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_change_password_username, container,
                false);
        setupFragment(fragment);
        return fragment;
    }

    private void setupFragment(View fragment) {
        mTextUsername = (TextInputEditText) fragment.findViewById(R.id.txt_change_password_username);
        mTextUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if (getActivity() instanceof ChangePasswordActivity) {
                        ChangePasswordActivity cpa = (ChangePasswordActivity)getActivity();
                        cpa.handleNextStep();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public String getUsername() {
        if (mTextUsername != null){
            return mTextUsername.getText().toString();
        }

        return null;
    }

}
