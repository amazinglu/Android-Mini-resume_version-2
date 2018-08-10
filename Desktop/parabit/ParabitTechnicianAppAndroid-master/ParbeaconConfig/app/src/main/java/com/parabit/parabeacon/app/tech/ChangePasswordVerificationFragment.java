package com.parabit.parabeacon.app.tech;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;


public class ChangePasswordVerificationFragment extends BaseFragment {

    private TextInputEditText mTextCode;

    public ChangePasswordVerificationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_change_password_verification,
                container, false);
        setupFragment(fragment);
        return fragment;
    }

    private void setupFragment(View fragment) {
        mTextCode = ((TextInputEditText) fragment.findViewById(R.id.txt_verification_code));
        mTextCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

    public String getVerificationCode() {
        if (mTextCode != null){
            return mTextCode.getText().toString();
        }

        return null;
    }

}
