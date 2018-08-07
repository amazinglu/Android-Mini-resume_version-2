package com.parabit.mmrbt.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by williamsnyder on 4/20/18.
 */

public interface AuthenticationService {
    @POST("company/authorize")
    Call<AuthenticationResponse> authenticate(@Body AuthenticationRequest request);
}
