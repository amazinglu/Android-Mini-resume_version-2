package com.parabit.mmrbt.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by williamsnyder on 9/8/17.
 */

public interface DoorControlService {

    @POST("register")
    Call<DeviceRegistrationResult> register(@Body DeviceRegistration deviceRegistration);

    @POST("unlock")
    Call<UnlockCommandResult> unlock(@Body UnlockCommand unlockCommand);

}
