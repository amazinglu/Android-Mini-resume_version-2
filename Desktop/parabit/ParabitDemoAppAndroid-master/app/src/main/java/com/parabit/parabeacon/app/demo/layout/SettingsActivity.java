package com.parabit.parabeacon.app.demo.layout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.state.AppState;
/**
 * Created by williamsnyder on 4/5/18.
 */

public class SettingsActivity extends BaseDemoActivity {

    private Button mButtonDuration;
    private Switch mCheckBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        mButtonDuration = (Button) findViewById(R.id.btn_door_time);
        mButtonDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDurationPrompt();
            }
        });

        //setup button to not show this again if found
        mCheckBox = (Switch) findViewById(R.id.switch_notify_doors);
        mCheckBox.setChecked(!getCurrentState().ignoreDoor());

        final SettingsActivity self = SettingsActivity.this;
        // Set up the user interaction to manually show or hide the system UI.
        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppState currentState = self.getCurrentState();
                currentState.setIgnoreDoor(!mCheckBox.isChecked());
                self.saveAppState();
                log().info("Ignoring door notifications:"+ currentState.ignoreDoor());
            }
        });

        getSupportActionBar().setHomeAsUpIndicator(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCheckBox.setChecked(!getCurrentState().ignoreDoor());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            onBackPressed();

            return true;
        }

        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showDurationPrompt() {
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(30);
        numberPicker.setMinValue(5);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Door Open Time");
        builder.setMessage("Set number seconds for the door to remain open :");
        builder.setView(numberPicker);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                int duration = numberPicker.getValue();
                mButtonDuration.setText(Integer.toString(duration) + " seconds");
                getCurrentState().setDoorOpenTime(duration);
                saveAppState();
                log().info("Door open duration now:" + duration + " seconds");
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

}
