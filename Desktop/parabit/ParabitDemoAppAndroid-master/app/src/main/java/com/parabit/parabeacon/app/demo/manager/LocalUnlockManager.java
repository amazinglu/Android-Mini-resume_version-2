package com.parabit.parabeacon.app.demo.manager;

import com.google.gson.Gson;
import com.parabit.mmrbt.api.UnlockCommand;
import com.parabit.mmrbt.api.UnlockCommandResult;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


/**
 * Created by williamsnyder on 3/16/18.
 */

public class LocalUnlockManager {

    private ConnectionFactory factory;
    private Gson gson = new Gson();
    private boolean waiting = true;


    public LocalUnlockManager(String url) {
        initRabbit(url);
    }

    public void unlock(final String serialNumber, final int doorOpenTime, final LocalUnlockCallback callback) {
        Thread rabbitThread = new Thread() {
            @Override
            public void run() {
                doUnlock(serialNumber, doorOpenTime, callback);
            }
        };
        rabbitThread.start();

    }

    public interface LocalUnlockCallback {
        void onUnlockSuccess();

        void onUnlockFailure();

        void onError();
    }

    /**
     * RabbitMQ is the tools that we use to talk with the distribute system
     * */
    private void initRabbit(String url) {
        factory = new ConnectionFactory();
        try {
            factory.setUri(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * talk to the server locally and get unlock response
     * */
    private void doUnlock(String serialNumber, int doorOpenTime, final LocalUnlockCallback callback) {
        try {
            UnlockCommand unlockCommand = new UnlockCommand();
            unlockCommand.setToken("123456");
            unlockCommand.setDeviceId("111222333");
            unlockCommand.setSerialNumber(serialNumber);
            unlockCommand.setDoorOpenTime(doorOpenTime);

            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            String message = "{\"request\":" + gson.toJson(unlockCommand) +  "}";
            channel.basicPublish("", "unlock", null, message.getBytes());
//            log("Local Sent:'" + message + "'");

            channel.basicConsume("unlock_response", new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                    log("Local Received:'" + new String(body) + "'");
                    String response =  new String(body);
                    UnlockCommandResult unlockCommandResult = gson.fromJson(response, UnlockCommandResult.class);
                    if (unlockCommandResult != null && unlockCommandResult.isUnlocked()) {
                        callback.onUnlockSuccess();
                        return;
                    }
                    waiting = false;
                    callback.onUnlockFailure();
                    return;
                }
            });

            Thread.sleep(5000);
            if (!waiting) {
                return;
            }
            callback.onUnlockFailure();

        } catch (Exception e) {
            callback.onError();
            e.printStackTrace();
        }
    }

}
