package com.parabit.beacon.firmware;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by williamsnyder on 10/12/17.
 */

public interface FirmwareInfoService {

    @GET("firmware/info")
    Call<FirmwareSummary> getSummary(@Query("revision") String revision);

    @POST("firmware/unlock")
    Call<FirmwareUnlockResponse> getUnlockResponse(@Body FirmwareUnlockRequest unlockRequest);
}
