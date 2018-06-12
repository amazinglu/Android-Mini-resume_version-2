package com.example.amazinglu.mini_resume.experience_edit;

import android.support.v4.app.Fragment;

import com.example.amazinglu.mini_resume.base.SingleFragmentActivity;

public class ExperienceEditActivity extends SingleFragmentActivity {
    @Override
    protected Fragment newFragment() {
        return ExperienceEditFragment.newInstance(getIntent().getExtras());
    }
}
