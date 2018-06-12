package com.example.amazinglu.recycler_view_function_demo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ListViewHolder extends RecyclerView.ViewHolder {

    public View mainView;
    public TextView text;

    public ListViewHolder(View itemView) {
        super(itemView);
        text = (TextView) itemView.findViewById(R.id.item_text);
        mainView = itemView;
    }
}
