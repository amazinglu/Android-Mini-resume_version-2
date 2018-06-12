package com.example.amazinglu.mini_resume.experience_edit;

import com.example.amazinglu.mini_resume.base.DeleteDialogFragment;

public class ExperienceDeleteDialog extends DeleteDialogFragment {

    public static final String TAG = "experience_delete_confirm_dialog";

    public static ExperienceDeleteDialog newInstance() {
        return new ExperienceDeleteDialog();
    }

    @Override
    protected int getRequestCode() {
        return ExperienceEditFragment.REQ_EXP_DELETE_CONFIRM_CODE;
    }
}
