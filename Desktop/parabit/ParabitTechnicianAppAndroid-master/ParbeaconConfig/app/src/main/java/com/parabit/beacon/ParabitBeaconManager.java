package com.parabit.beacon;

import com.parabit.beacon.api.BeaconInfo;
import com.parabit.beacon.api.BeaconInfoService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by williamsnyder on 9/4/17.
 */

public class ParabitBeaconManager {

    private static ParabitBeaconManager instance;
    private BeaconInfoService service;
    private String beaconAPIToken;
    private String beaconAPI;
    private String appId;

    private Map<String,BeaconInfo> parabeacons = new HashMap<String,BeaconInfo>();

    private ParabitBeaconManager(String appId, String url, String token) throws Exception{
        initialize(appId, url, token);
    }

    public static ParabitBeaconManager getInstance(String appId, String url, String token) throws Exception {
        if (instance == null) {
            instance = new ParabitBeaconManager(appId, url, token);
        }
        return instance;
    }

    public void getParabitBeacon(String namespace, String instanceId, Callback<BeaconInfo> callback) {
        Call<BeaconInfo> getBeacon_call = service.getBeacon(namespace, instanceId);
        getBeacon_call.enqueue(callback);
    }

    public void getParabitBeacon(String serialNumber, Callback<BeaconInfo> callback) {
        Call<BeaconInfo> getBeacon_call = service.getBeaconBySerialNumber(serialNumber);
        getBeacon_call.enqueue(callback);
    }

    public void updateBeacon(BeaconInfo beaconInfo, Callback<BeaconInfo> callback) {
        Call<BeaconInfo> updateBeacon_call = service.updateBeacon(beaconInfo.getUuid(),beaconInfo);
        updateBeacon_call.enqueue(callback);
    }

    public void registerBeacon(BeaconInfo beaconInfo, Callback<BeaconInfo> callback) {
        Call<BeaconInfo> registerBeacon_call = service.registerBeacon(beaconInfo);
        registerBeacon_call.enqueue(callback);
    }

    private String getBeaconAPIToken(){
        return this.beaconAPIToken;
    }

    private String getBeaconAPI(){
        return this.beaconAPI;
    }

    private String getAppId(){
        return this.appId;
    }

    private BeaconInfoService getBeaconInfoService() throws Exception{

        if (getBeaconAPIToken() == null || getBeaconAPI() == null) {
            throw new Exception("Unable to connect to beacon service");
        }

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request.Builder ongoing = chain.request().newBuilder();
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("x-api-key", getBeaconAPIToken());
                        ongoing.addHeader("x-app-id", getAppId());
                        return chain.proceed(ongoing.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(getBeaconAPI())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(BeaconInfoService.class);
    }

    private void initialize(String appId, String url, String token) throws Exception {
        this.appId = appId;
        this.beaconAPI = url;
        this.beaconAPIToken = token;

        service = getBeaconInfoService();

    }

}
