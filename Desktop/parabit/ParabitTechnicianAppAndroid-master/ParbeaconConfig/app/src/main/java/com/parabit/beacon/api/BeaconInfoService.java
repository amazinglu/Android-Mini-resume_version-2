package com.parabit.beacon.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by williamsnyder on 9/8/17.
 */

public interface BeaconInfoService {
    @GET("beacons")
    Call<List<BeaconInfo>> listBeacons();

    @GET("beacons/{uuid}")
    Call<BeaconInfo> getBeacon(@Path("uuid") String uuid);

    @GET("beacons/serial_number/{serialNumber}")
    Call<BeaconInfo> getBeaconBySerialNumber(@Path("serialNumber") String serialNumber);

    @GET("beacons/namespace/{namespaceId}/instance/{instanceId}")
    Call<BeaconInfo> getBeacon(@Path("namespaceId") String namespaceId,
                               @Path("instanceId") String instanceId);

    @POST("beacons")
    Call<BeaconInfo> registerBeacon(@Body BeaconInfo beacon);

    @PUT("beacons/{uuid}")
    Call<BeaconInfo> updateBeacon(@Path("uuid") String uuid, @Body BeaconInfo beaconInfo);

    @DELETE("beacons/{uuid}")
    Call<Object> deleteBeacon(@Path("uuid") String uuid);

}
