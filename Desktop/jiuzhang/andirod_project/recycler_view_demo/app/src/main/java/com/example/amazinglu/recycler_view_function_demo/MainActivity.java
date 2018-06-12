package com.example.amazinglu.recycler_view_function_demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: recycler view 侧滑菜单， 侧滑删除（左面和右面） (可以根据view type自定义菜单)
        //TODO: recycler view 拖曳变化位置
        //TODO: recycler view header and footer view
        //TODO: recycler view 拖曳变化位置

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, ListFragment.newInstance())
                    .commit();
        }
    }
}
