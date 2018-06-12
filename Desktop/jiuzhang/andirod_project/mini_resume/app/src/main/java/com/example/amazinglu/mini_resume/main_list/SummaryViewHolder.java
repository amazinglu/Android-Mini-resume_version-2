package com.example.amazinglu.mini_resume.main_list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.amazinglu.mini_resume.R;

public class SummaryViewHolder extends RecyclerView.ViewHolder {

    public TextView userName, userEmail, userphoneNum;
    public ImageView userImage;
    public ImageButton summaryEdit;

    public SummaryViewHolder(View itemView) {
        super(itemView);

        userName = (TextView) itemView.findViewById(R.id.user_name);
        userEmail = (TextView) itemView.findViewById(R.id.user_email);
        userphoneNum = (TextView) itemView.findViewById(R.id.user_phone_num);
        summaryEdit = (ImageButton) itemView.findViewById(R.id.summary_edit);
        userImage = (ImageView) itemView.findViewById(R.id.user_image);
    }
}
