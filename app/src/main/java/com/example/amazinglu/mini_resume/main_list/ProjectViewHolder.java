package com.example.amazinglu.mini_resume.main_list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.amazinglu.mini_resume.R;

public class ProjectViewHolder extends RecyclerView.ViewHolder {

    public TextView projectName, proDuration, proDescription, proUrl;
    public ImageButton proEdit;

    public ProjectViewHolder(View itemView) {
        super(itemView);

        projectName = (TextView) itemView.findViewById(R.id.project_name);
        proDuration = (TextView) itemView.findViewById(R.id.project_duration);
        proDescription = (TextView) itemView.findViewById(R.id.project_description);
        proUrl = (TextView) itemView.findViewById(R.id.project_url);
        proEdit = (ImageButton) itemView.findViewById(R.id.project_edit);
    }
}
