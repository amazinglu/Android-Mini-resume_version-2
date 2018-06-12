package com.example.amazinglu.recycler_view_function_demo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.amazinglu.recycler_view_function_demo.model.Element;

import java.util.List;

public class Adapter extends RecyclerView.Adapter {

    private List<Element> data;
    private Context context;

    public Adapter(List<Element> data) {
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view  = LayoutInflater.from(getContext())
                .inflate(R.layout.item_recycler_view, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ListViewHolder viewHolder = (ListViewHolder) holder;
        viewHolder.text.setText(data.get(position).text);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public Context getContext() {
        return context;
    }
}
