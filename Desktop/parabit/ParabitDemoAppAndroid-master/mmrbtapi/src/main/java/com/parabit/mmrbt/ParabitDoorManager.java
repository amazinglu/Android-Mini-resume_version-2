package com.parabit.mmrbt;


import com.parabit.mmrbt.api.DeviceRegistration;
import com.parabit.mmrbt.api.DeviceRegistrationResult;
import com.parabit.mmrbt.api.DoorControlService;
import com.parabit.mmrbt.api.UnlockCommand;
import com.parabit.mmrbt.api.UnlockCommandResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class ParabitDoorManager {

    private static ParabitDoorManager instance;
    private DoorControlService service;

    private String controlAPIToken;
    private String controlAPI;
    private String appId;

    private Map<String,ParabitBeacon> parabeacons = new HashMap<String,ParabitBeacon>();

    private ParabitDoorManager(String url, String token, String appId) {
        initialize(url, token, appId);
    }

    public static ParabitDoorManager getInstance(String url, String token, String appId) {
        if (instance == null) {
            instance = new ParabitDoorManager(url, token, appId);
        }
        return instance;
    }

    public void register(DeviceRegistration registration, Callback<DeviceRegistrationResult> callback) {
        Call<DeviceRegistrationResult> call = service.register(registration);
        call.enqueue(callback);
    }

    public void unlock(UnlockCommand unlockCommand, Callback<UnlockCommandResult> callback) {
        /**
         * set post request to the server to unlock the door
         * */
        Call<UnlockCommandResult> call = service.unlock(unlockCommand);
        call.enqueue(callback);
    }

    private String getControlAPIToken(){
        return this.controlAPIToken;
    }

    private String getControlAPI(){
        return this.controlAPI;
    }

    private DoorControlService getDoorControlService() throws Exception{
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request.Builder ongoing = chain.request().newBuilder();
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("x-api-key", getControlAPIToken());
                        ongoing.addHeader("x-app-id", appId);
                        return chain.proceed(ongoing.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(getControlAPI())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(DoorControlService.class);
    }

    private void initialize(String url, String token, String appId) {
        this.controlAPI = url;
        this.controlAPIToken = token;
        this.appId = appId;

        try {
            service = getDoorControlService();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
