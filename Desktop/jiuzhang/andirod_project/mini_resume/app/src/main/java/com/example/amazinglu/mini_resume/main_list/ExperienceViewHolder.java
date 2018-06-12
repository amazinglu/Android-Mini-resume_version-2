package com.example.amazinglu.mini_resume.main_list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.amazinglu.mini_resume.R;

public class ExperienceViewHolder extends RecyclerView.ViewHolder {

    public TextView companyName, workTitle, expDuration, expDescription;
    public ImageButton expEdit;

    public ExperienceViewHolder(View itemView) {
        super(itemView);

        companyName = (TextView) itemView.findViewById(R.id.workplace_name);
        workTitle = (TextView) itemView.findViewById(R.id.work_title);
        expDuration = (TextView) itemView.findViewById(R.id.experience_duration);
        expDescription = (TextView) itemView.findViewById(R.id.experience_description);
        expEdit = (ImageButton) itemView.findViewById(R.id.experience_edit);
    }
}
