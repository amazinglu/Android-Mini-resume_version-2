package com.parabit.mmrbt.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BankLocationService {

    @GET("branch")
    Call<List<BankLocation>> getAllLocations();

    @GET("branch")
    Call<List<BankLocation>> getLocationsNearby(@Query("lat") double lat,
                                                @Query("lon") double lon,
                                                @Query("format") String format);
    @GET("branch/{locationId}")
    Call<BankLocation> getBankLocation(@Path("locationId") String locationId);
}
