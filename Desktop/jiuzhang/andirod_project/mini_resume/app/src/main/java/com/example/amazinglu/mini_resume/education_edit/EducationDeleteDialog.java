package com.example.amazinglu.mini_resume.education_edit;

import com.example.amazinglu.mini_resume.R;
import com.example.amazinglu.mini_resume.base.DeleteDialogFragment;

public class EducationDeleteDialog extends DeleteDialogFragment {
    public static final String TAG = "education_delete_confirm_dialog";

    public static  EducationDeleteDialog newInstance() {
        return new EducationDeleteDialog();
    }

    @Override
    protected int getRequestCode() {
        return EducationEditFragment.REQ_EDU_DELETE_CONFIRM_CODE;
    }
}
