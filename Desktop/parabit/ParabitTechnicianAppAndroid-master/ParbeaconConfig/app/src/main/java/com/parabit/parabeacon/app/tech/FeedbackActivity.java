package com.parabit.parabeacon.app.tech;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class FeedbackActivity extends BaseActivity {

    enum Mode {
        General,
        Problem
    }

    private Mode mode = Mode.General;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_feedback);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        int category = getIntent().getIntExtra("category",0);
        if (category == 1) {
            mode = Mode.Problem;
            setTitle("Report a Problem");
            ((EditText)findViewById(R.id.txt_feedback)).setHint("Tell us what went wrong.");
        }

        Fragment f = getSupportFragmentManager().getFragments().get(0);
        if (f instanceof FeedbackFragment) {
            ((FeedbackFragment)f).setMode(mode);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_send_feedback:
                sendFeedback();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendFeedback() {
        if (getSupportFragmentManager().getFragments().size() != 1) {
            return;
        }
        Fragment f = getSupportFragmentManager().getFragments().get(0);
        if (f instanceof FeedbackFragment) {
            ((FeedbackFragment)f).sendFeedback(mode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

}
