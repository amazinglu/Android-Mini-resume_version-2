package com.parabit.mmrbt.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by williamsnyder on 3/12/18.
 */

public class BankLocationManager {

    private static BankLocationManager instance;

    private BankLocationService service;

    private String branchAPIToken;
    private String branchAPI;
    private String appId;

    private final String MILES_FORMAT = "miles";

    private List<BankLocation> locations = new ArrayList<>();

    private BankLocationManager(String url, String token, String appId) {
        initialize(url, token, appId);
    }

    public static BankLocationManager getInstance(String url, String token, String appId) {
        if (instance == null) {
            instance = new BankLocationManager(url, token, appId);
        }

        return instance;
    }

    public void getNearbyLocations(double lat, double lon, Callback<List<BankLocation>> callback) {
        Call<List<BankLocation>> call = service.getLocationsNearby(lat, lon, MILES_FORMAT);
        call.enqueue(callback);
    }

    public void getBankLocation(String locationId, Callback<BankLocation> callback) {
        Call<BankLocation> call = service.getBankLocation(locationId);
        call.enqueue(callback);
    }

    private String getBranchAPIToken(){
        return this.branchAPIToken;
    }

    private String getBranchAPI(){
        return this.branchAPI;
    }

    private BankLocationService getBranchService() throws Exception{
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request.Builder ongoing = chain.request().newBuilder();
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("x-api-key", getBranchAPIToken());
                        return chain.proceed(ongoing.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(getBranchAPI())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(BankLocationService.class);
    }

    private void initialize(String url, String token, String appId) {
        this.branchAPI = url;
        this.branchAPIToken = token;
        this.appId = appId;

        try {
            service = getBranchService();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
