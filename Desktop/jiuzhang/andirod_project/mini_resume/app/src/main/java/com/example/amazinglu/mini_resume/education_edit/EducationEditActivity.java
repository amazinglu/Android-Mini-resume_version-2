package com.example.amazinglu.mini_resume.education_edit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.example.amazinglu.mini_resume.base.SingleFragmentActivity;

public class EducationEditActivity extends SingleFragmentActivity {

    private EducationEditFragment fragment;

    @Override
    protected Fragment newFragment() {
        this.fragment = EducationEditFragment.newInstance(getIntent().getExtras());
        return this.fragment;
    }
}
