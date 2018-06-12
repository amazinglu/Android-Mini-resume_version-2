package com.example.amazinglu.mini_resume.main_list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.amazinglu.mini_resume.R;

public class EducationViewHolder extends RecyclerView.ViewHolder {

    public TextView school_name, major, eduDescription, educationDuration, gpa;
    public ImageButton educationEdit;

    public EducationViewHolder(View itemView) {
        super(itemView);

        school_name = (TextView) itemView.findViewById(R.id.school_name);
        major = (TextView) itemView.findViewById(R.id.major);
        educationDuration = (TextView) itemView.findViewById(R.id.education_duration);
        eduDescription = (TextView) itemView.findViewById(R.id.education_description);
        gpa = (TextView) itemView.findViewById(R.id.gpa);
        educationEdit = (ImageButton) itemView.findViewById(R.id.education_edit);
    }
}
