package com.example.amazinglu.mini_resume.summary_edit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.example.amazinglu.mini_resume.base.SingleFragmentActivity;

public class SummaryEditActivity extends SingleFragmentActivity {
    @Override
    protected Fragment newFragment() {
        return SummaryEditFragment.newInstance(getIntent().getExtras());
    }
}
