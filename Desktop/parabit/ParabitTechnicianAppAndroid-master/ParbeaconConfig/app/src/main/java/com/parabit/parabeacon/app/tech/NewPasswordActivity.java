package com.parabit.parabeacon.app.tech;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class NewPasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Account Setup");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_set_new_password:
                setPassword();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setPassword() {
        if (getSupportFragmentManager().getFragments().size() != 1) {
            return;
        }
        Fragment f = getSupportFragmentManager().getFragments().get(0);
        if (f instanceof NewPasswordFragment) {
            ((NewPasswordFragment)f).submitPassword();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_password, menu);
        return true;
    }


}
