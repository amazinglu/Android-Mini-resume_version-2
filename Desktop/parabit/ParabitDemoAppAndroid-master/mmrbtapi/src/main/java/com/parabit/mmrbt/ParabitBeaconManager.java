package com.parabit.mmrbt;


import com.parabit.mmrbt.api.BeaconInfo;
import com.parabit.mmrbt.api.BeaconInfoService;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
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

    private ParabitBeaconManager(String url, String token, String appId) {
        initialize(url, token, appId);
    }

    public static ParabitBeaconManager getInstance(String url, String token, String appId) {
        if (instance == null) {
            instance = new ParabitBeaconManager(url, token, appId);
        }
        return instance;
    }

    public void getBeaconBySerialNumber(int serialNumber, Callback<BeaconInfo> callback) {
        Call<BeaconInfo> call = service.getBeaconBySerialNumber(serialNumber);
        call.enqueue(callback);
    }

    private String getBeaconAPIToken(){
        return this.beaconAPIToken;
    }

    private String getBeaconAPI(){
        return this.beaconAPI;
    }

    public String getAppId() {
        return appId;
    }

    private BeaconInfoService getBeaconInfoService() throws Exception{
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

    private void initialize(String url, String token, String appId) {
        this.beaconAPI = url;
        this.beaconAPIToken = token;
        this.appId = appId;

        try {
            service = getBeaconInfoService();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
