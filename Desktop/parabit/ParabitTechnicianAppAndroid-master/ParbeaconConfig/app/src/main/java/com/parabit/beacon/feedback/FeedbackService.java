package com.parabit.beacon.feedback;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by williamsnyder on 11/27/17.
 */


public interface FeedbackService {

    @POST("feedback")
    Call<Feedback> sendFeedback(@Body Feedback feedback);
}
