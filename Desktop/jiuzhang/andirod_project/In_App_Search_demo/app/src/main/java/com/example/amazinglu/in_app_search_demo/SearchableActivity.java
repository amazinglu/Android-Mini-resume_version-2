package com.example.amazinglu.in_app_search_demo;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends AppCompatActivity {

    public static final String KEY_DATA = "data";

    private List<String> data;
    private List<String> unfilterData;
    private SearchResultAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        handleIntent(getIntent());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        data = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.search_result_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchableActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(SearchableActivity.this, DividerItemDecoration.VERTICAL));
        adapter = new SearchResultAdapter(data);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        Toast.makeText(SearchableActivity.this, "handleIntent", Toast.LENGTH_SHORT).show();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            // get user data
            Bundle bundle = getIntent().getBundleExtra(SearchManager.APP_DATA);
            if (bundle != null) {
                unfilterData = bundle.getStringArrayList(KEY_DATA);
            }

            doSearch(query);
        }
    }

    private void doSearch(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                data.clear();
                for (String str : unfilterData) {
                    if (str.toLowerCase().contains(query.toLowerCase())) {
                        data.add(str);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
}
