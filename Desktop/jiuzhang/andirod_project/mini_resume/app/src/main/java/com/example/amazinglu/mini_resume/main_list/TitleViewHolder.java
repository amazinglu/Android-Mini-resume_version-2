package com.example.amazinglu.mini_resume.main_list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.amazinglu.mini_resume.R;

public class TitleViewHolder extends RecyclerView.ViewHolder {

    public TextView headerName;
    public ImageButton add;

    public TitleViewHolder(View itemView) {
        super(itemView);

        headerName = (TextView) itemView.findViewById(R.id.haeder_name);
        add = (ImageButton) itemView.findViewById(R.id.add);
    }
}
