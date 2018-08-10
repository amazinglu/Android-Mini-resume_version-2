package com.parabit.beacon;

import com.parabit.beacon.firmware.FirmwareInfoService;
import com.parabit.beacon.firmware.FirmwareSummary;
import com.parabit.beacon.firmware.FirmwareUnlockRequest;
import com.parabit.beacon.firmware.FirmwareUnlockResponse;
import com.parabit.parabeacon.app.tech.utils.Utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by williamsnyder on 10/12/17.
 */

public class ParabitFirmwareManager {

    private FirmwareInfoService firmwareInfoService;
    private static ParabitFirmwareManager instance;
    private String firmwareAPIToken;
    private String firmwareAPI;

    private ParabitFirmwareManager(String url, String token) throws Exception {
        initialize( url,  token);
    }

    public static ParabitFirmwareManager getInstance(String url, String token) throws Exception {
        if (instance == null) {
            instance = new ParabitFirmwareManager( url,  token);
        }
        return instance;
    }

    public void getFirmwareSummary(String revision, Callback<FirmwareSummary> callback) {
        Call<FirmwareSummary> firmwareSummary_call = firmwareInfoService.getSummary(revision);
        firmwareSummary_call.enqueue(callback);
    }

    public void getUnlockResponse(String revision, byte[] unlockChallenge, Callback<FirmwareUnlockResponse> callback) {
        FirmwareUnlockRequest unlockRequest = new FirmwareUnlockRequest();
        unlockRequest.setChallenge(Utils.byteArrayToHexString(unlockChallenge));
        unlockRequest.setFirmwareRevision(revision);
        Call<FirmwareUnlockResponse> firmwareSummary_call = firmwareInfoService.getUnlockResponse(unlockRequest);
        firmwareSummary_call.enqueue(callback);
    }

    private void initialize(String url, String token) throws Exception {
        this.firmwareAPI = url;
        this.firmwareAPIToken = token;
        firmwareInfoService = getFirmwareInfoService();
    }

    private String getFirmwareAPIToken(){
        return this.firmwareAPIToken;
    }

    private String getFirmwareAPI(){
        return this.firmwareAPI;
    }

    private FirmwareInfoService getFirmwareInfoService() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder ongoing = chain.request().newBuilder();
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("x-api-key", getFirmwareAPIToken());
                        return chain.proceed(ongoing.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(getFirmwareAPI())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(FirmwareInfoService.class);
    }
}
