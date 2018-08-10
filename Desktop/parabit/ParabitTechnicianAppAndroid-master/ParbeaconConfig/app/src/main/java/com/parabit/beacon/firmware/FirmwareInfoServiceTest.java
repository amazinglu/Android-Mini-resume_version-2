package com.parabit.beacon.firmware;

import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by williamsnyder on 10/12/17.
 */

public class FirmwareInfoServiceTest {

    public static void main(String[] args) {
        Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("http://localhost:3000")
                .baseUrl("https://6yomwzar14.execute-api.us-east-1.amazonaws.com/dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FirmwareInfoService service = retrofit.create(FirmwareInfoService.class);

        try {
            String version = "10-10-17";
            Call<FirmwareSummary> firmwareSummary_call = service.getSummary(version);
            Response<FirmwareSummary> firmwareSummary_response = firmwareSummary_call.execute();
            if (firmwareSummary_response.isSuccessful()) {
                FirmwareSummary summary = firmwareSummary_response.body();
                System.out.println(summary.getLatestURL());
                System.out.println(summary.getCurrent().getRevision());
                System.out.println(summary.getCurrent().getUnlockCode());
                System.exit(0);
                return;
            } else {
                System.out.println(firmwareSummary_response.errorBody().string());
                System.exit(0);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
