package com.example.amazinglu.in_app_search_demo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class SearchResultViewHolder extends RecyclerView.ViewHolder {

    public TextView text;

    public SearchResultViewHolder(View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.search_result_item);
    }
}
