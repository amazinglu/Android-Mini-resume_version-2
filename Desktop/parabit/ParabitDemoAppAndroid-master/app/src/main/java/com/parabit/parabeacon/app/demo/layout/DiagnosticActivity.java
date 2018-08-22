package com.parabit.parabeacon.app.demo.layout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.parabit.mmrbt.ParabitBeaconSDK;
import com.parabit.mmrbt.api.UnlockCommand;
import com.parabit.mmrbt.api.UnlockHandler;
import com.parabit.parabeacon.app.demo.R;
import com.parabit.parabeacon.app.demo.state.AppState;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Created by williamsnyder on 3/16/18.
 */

public class DiagnosticActivity extends BaseDemoActivity {

    private TextView txtRabbitLog;
    private TextInputLayout txtIPAddress;
    private Switch mSwitchLocal;
    private Switch mSwitchDemo;
    private Button mButtonDoorTime;
    private boolean useLocal = false;
    private String localIpAddress;
    private Gson gson = new Gson();
    private SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS");
    private boolean useDemo = false;
    private int duration = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activty_diagnostics);

        mButtonDoorTime = (Button) findViewById(R.id.btn_door_open_time);
        mButtonDoorTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDurationPrompt();
            }
        });

        Button mButtonTest = (Button) findViewById(R.id.btnTestUnlock);
        mButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickDoor();
            }
        });

        txtRabbitLog = (TextView) findViewById(R.id.txt_rabbit_log);
        txtRabbitLog.setMovementMethod(new ScrollingMovementMethod());

        txtIPAddress = (TextInputLayout) findViewById(R.id.txt_local_ip);
        mSwitchLocal = (Switch) findViewById(R.id.switch_use_local);
        mSwitchLocal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                useLocal = buttonView.isChecked();
                txtIPAddress.setEnabled(useLocal);
                getCurrentState().setDemoMode(true);
                saveAppState();
            }
        });

        mSwitchDemo = (Switch) findViewById(R.id.switch_use_demo);

        mSwitchDemo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                useDemo = buttonView.isChecked();
                mSwitchLocal.setChecked(true);
                mSwitchLocal.setEnabled(!useDemo);
                txtIPAddress.setEnabled(!useDemo);
                txtIPAddress.getEditText().setText(R.string.local_rabbit_url);
                AppState currentState = getCurrentState();
                currentState.setDemoMode(useDemo);
                getAppStateManager().update(currentState);
            }
        });

        mSwitchDemo.setChecked(getCurrentState().isDemoMode());

        getSupportActionBar().setHomeAsUpIndicator(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSwitchDemo.setChecked(getCurrentState().isDemoMode());
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

    private void handleClickDoor() {

        if (mSwitchLocal.isChecked()) {
            Thread rabbitThread = new Thread() {
                @Override
                public void run() {
                    unlockViaRabbit();
                }
            };
            rabbitThread.start();
            return;
        }

        String serialNumber = "";
        log("Remote: Sending request");
        ParabitBeaconSDK.unlock(serialNumber, duration, new UnlockHandler() {
            @Override
            public void onResult(boolean unlocked) {
                updateDoorStatus(unlocked);
            }

            @Override
            public void onError(String s) {
                System.out.println("Oops:"+ s);
                log("Remote: Error:" + s);
            }
        });
        return;
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
                duration = numberPicker.getValue();
                mButtonDoorTime.setText(Integer.toString(duration) + " seconds");
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    private void updateDoorStatus(final boolean unlocked) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = unlocked ? "Door is unlocked." : "Door is locked";
                Toast toast = Toast.makeText(getApplicationContext(),
                        message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 80);
                toast.show();
                log("Remote: " + message);
            }
        });

    }

    /**
     * it is a fake test
     * */
    private void unlockViaRabbit() {

        try {
            final UnlockCommand unlockCommand = new UnlockCommand();
            unlockCommand.setToken("123456");
            unlockCommand.setDeviceId("00000000");
            unlockCommand.setSerialNumber("123456");
            unlockCommand.setDoorOpenTime(duration);

            localIpAddress = txtIPAddress.getEditText().getText().toString().trim();
            ConnectionFactory factory = new ConnectionFactory();

            if (localIpAddress.startsWith("amqp")) {
                factory.setUri(localIpAddress);
            } else {
                factory.setUri("amqp://guest:guest@"+localIpAddress+":5672");
            }

            log("Local: Connecting to:" + factory.getHost());

            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            String message = "{\"request\":" + gson.toJson(unlockCommand) +  "}";
            channel.basicPublish("", "unlock", null, message.getBytes());
            log("Local Sent:'" + message + "'");

            channel.basicConsume("unlock_response", new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String response = new String(body);
                    log("Local Received:'" + response + "'");
                }

                @Override
                public void handleCancel(String consumerTag) throws IOException {
                    super.handleCancel(consumerTag);
                    log("Local Received Cancel:'" + consumerTag + "'");
                }

                @Override
                public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                    super.handleShutdownSignal(consumerTag, sig);
                    log("Local Received Shutdown:'" + consumerTag + "' ::" + sig.getMessage());
                }
            });
        } catch (Exception e) {
            log("Unable to connect:"+e.getMessage());
            e.printStackTrace();
        }

    }

    private void log(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String date = format.format(new Date());
                txtRabbitLog.append("\n["+ date + "] " + message);
            }
        });

    }
}
