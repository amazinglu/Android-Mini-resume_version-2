package com.parabit.mmrbt.api;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by williamsnyder on 9/9/17.
 */

public class BeaconInfoServiceTest {
    public static void main(String[] args) {
        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("http://localhost:3000")
                .baseUrl("https://aui7iwreg4.execute-api.us-east-1.amazonaws.com/dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BeaconInfoService service = retrofit.create(BeaconInfoService.class);

        BeaconInfo beaconInfo = new BeaconInfo();
        beaconInfo.setName("From code");
        beaconInfo.setLocation("Android Studio");
        beaconInfo.setMacAddress("123:456");
        beaconInfo.setSerialNumber("111AAA");

        try {
            Call<BeaconInfo> registerBeacon_call = service.registerBeacon(beaconInfo);
            Response<BeaconInfo> registerBeacon_response = registerBeacon_call.execute();
            if (registerBeacon_response.isSuccessful()) {
                beaconInfo = registerBeacon_response.body();
            } else {
                System.out.println(registerBeacon_response.errorBody().string());
                System.exit(0);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Call<BeaconInfo> getBeacon_call = service.getBeacon(beaconInfo.getUuid());
        try {
            BeaconInfo beacon = getBeacon_call.execute().body();
            System.out.println("*"+beacon.toJSON());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            beaconInfo.setName("Bill");
            Call<BeaconInfo> updateBeacon_call = service.updateBeacon(beaconInfo.getUuid(),beaconInfo);
            Response<BeaconInfo> updateBeacon_response = updateBeacon_call.execute();
            if (updateBeacon_response.isSuccessful()) {
                beaconInfo = updateBeacon_response.body();
            } else {
                System.out.println("**:"+updateBeacon_response.errorBody().string());
                System.exit(0);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Call<List<BeaconInfo>> listBeacons_call = service.listBeacons();
        try {
            List<BeaconInfo> beacons = listBeacons_call.execute().body();
            for(BeaconInfo beacon: beacons) {
                System.out.println(beacon.toJSON());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Call<Object> deleteBeacon_call = service.deleteBeacon(beaconInfo.getUuid());
            Response<Object> deleteBeacon_response = deleteBeacon_call.execute();
            if (deleteBeacon_response.isSuccessful()) {
                System.out.println("Beacon deleted");
            } else {
                System.out.println("**:"+deleteBeacon_response.errorBody().string());
                System.exit(0);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        getBeacon_call = service.getBeacon(beaconInfo.getUuid());
        try {
            Response<BeaconInfo> getBeacon_response = getBeacon_call.execute();
            if (getBeacon_response.isSuccessful()) {
                System.out.println("beacon should have been deleted");
            } else {
                System.out.println("Expected: Beacon deleted: " + getBeacon_response.errorBody().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}
