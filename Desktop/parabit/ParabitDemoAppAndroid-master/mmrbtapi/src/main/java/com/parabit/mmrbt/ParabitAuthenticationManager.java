package com.parabit.mmrbt;


import com.parabit.mmrbt.api.AuthenticationRequest;
import com.parabit.mmrbt.api.AuthenticationResponse;
import com.parabit.mmrbt.api.AuthenticationService;

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

public class ParabitAuthenticationManager {

    private static ParabitAuthenticationManager instance;
    private AuthenticationService service;

    private String authAPIToken;
    private String authAPI;

    private Map<String,ParabitBeacon> parabeacons = new HashMap<String,ParabitBeacon>();

    private ParabitAuthenticationManager(String url, String token) {
        initialize(url, token);
    }

    public static ParabitAuthenticationManager getInstance(String url, String token) {
        if (instance == null) {
            instance = new ParabitAuthenticationManager(url, token);
        }
        return instance;
    }

    public void authenticate(AuthenticationRequest request, Callback<AuthenticationResponse> callback) {
        Call<AuthenticationResponse> call = service.authenticate(request);
        call.enqueue(callback);
    }


    private String getAuthAPIToken(){
        return this.authAPIToken;
    }

    private String getAuthAPI(){
        return this.authAPI;
    }

    private AuthenticationService getAuthenticationService() throws Exception{
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request.Builder ongoing = chain.request().newBuilder();
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("x-api-key", getAuthAPIToken());
                        return chain.proceed(ongoing.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(getAuthAPI())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(AuthenticationService.class);
    }

    private void initialize(String url, String token) {
        this.authAPI = url;
        this.authAPIToken = token;

        try {
            service = getAuthenticationService();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
