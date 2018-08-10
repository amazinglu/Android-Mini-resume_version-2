package com.parabit.beacon;

import com.parabit.beacon.feedback.FeedbackService;
import com.parabit.beacon.feedback.Feedback;
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
 * Created by williamsnyder on 11/27/17.
 */

public class ParabitFeedbackManager {

    private FeedbackService feedbackService;
    private static ParabitFeedbackManager instance;
    private String feedbackAPIToken;
    private String feedbackAPI;

    private ParabitFeedbackManager(String url, String token) throws Exception {
        initialize( url,  token);
    }

    public static ParabitFeedbackManager getInstance(String url, String token) throws Exception {
        if (instance == null) {
            instance = new ParabitFeedbackManager( url,  token);
        }
        return instance;
    }

    public void sendFeedback(Feedback feedback, Callback<Feedback> callback) {
        Call<Feedback> feedbackSummary_call = feedbackService.sendFeedback(feedback);
        feedbackSummary_call.enqueue(callback);
    }

    private void initialize(String url, String token) throws Exception {
        this.feedbackAPI = url;
        this.feedbackAPIToken = token;
        feedbackService = getFeedbackService();
    }

    private String getFeedbackAPIToken(){
        return this.feedbackAPIToken;
    }

    private String getFeedbackAPI(){
        return this.feedbackAPI;
    }

    private FeedbackService getFeedbackService() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder ongoing = chain.request().newBuilder();
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("x-api-key", getFeedbackAPIToken());
                        return chain.proceed(ongoing.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(getFeedbackAPI())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(FeedbackService.class);
    }
}
