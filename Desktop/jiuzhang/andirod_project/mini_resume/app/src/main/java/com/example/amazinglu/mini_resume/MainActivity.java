package com.example.amazinglu.mini_resume;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.amazinglu.mini_resume.main_list.MainListFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create the fragment
        // the layout setup is in the fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, MainListFragment.newInstance())
                    .commit();
        }
    }
}
