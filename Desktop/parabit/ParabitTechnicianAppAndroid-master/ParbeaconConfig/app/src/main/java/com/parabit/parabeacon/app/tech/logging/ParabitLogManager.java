package com.parabit.parabeacon.app.tech.logging;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.parabit.parabeacon.app.tech.BuildConfig;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;

/**
 * Created by williamsnyder on 12/15/17.
 */

public class ParabitLogManager {

    private static ParabitLogManager instance;
    private LogService logService;
    private String logAPIToken;
    private String logAPI;
    private boolean isDebugging;
    private Map<String, String> defaultProps = new HashMap<>();

    private ParabitLogManager(String url, String token) throws Exception {
        initialize( url,  token);
    }

    public static ParabitLogManager getInstance(String url, String token) throws Exception {
        if (instance == null) {
            instance = new ParabitLogManager(url, token);
        }
        return instance;
    }

    public void logEvent(String message) {
        logAsync(LogData.Level.EVENT,message,null);
    }

    public void logEvent(String message, Map<String, String> attributes) {
        logAsync(LogData.Level.EVENT, message, attributes);
    }

    public void logWarn(String message) {
        logAsync(LogData.Level.WARN,message,null);
    }

    public void logWarn(String message, Map<String, String> attributes) {
        logAsync(LogData.Level.WARN, message, attributes);
    }

    public void logError(String message) {
        logAsync(LogData.Level.ERROR,message,null);
    }

    public void logError(String message, Map<String, String> attributes) {
        logAsync(LogData.Level.ERROR, message, attributes);
    }

    public void logFatal(String message) {
        logAsync(LogData.Level.FATAL,message, null);
    }

    public void logFatal(String message, Map<String, String> attributes) {
        logAsync(LogData.Level.FATAL, message, attributes);
    }

    public void logDebug(String message) {
        logAsync(LogData.Level.DEBUG,message, null);
    }

    public void logDebug(String message, Map<String, String> attributes) {
        logAsync(LogData.Level.DEBUG, message, attributes);
    }

    public void logInfo(String message) {
        logAsync(LogData.Level.INFO,message, null);
    }

    public void logInfo(String message, Map<String, String> attributes) {
        logAsync(LogData.Level.INFO, message, attributes);
    }

    private void logAsync(LogData.Level level, String message, Map<String,String> attributes) {
        if (logService == null) {
            return;
        }

        if (level == LogData.Level.DEBUG && !isDebugging) {
            return;
        }

        if (attributes == null) {
            attributes = defaultProps;
        } else if (!attributes.containsKey(Keys.USERNAME)) {
            attributes.put(Keys.USERNAME, defaultProps.get(Keys.USERNAME));
        }

        if (BuildConfig.DEBUG) {
            attributes.put(Keys.ENVIRONMENT, "development");
        } else {
            attributes.put(Keys.ENVIRONMENT, "production");
        }

        LogData logData = new LogData(level, message, attributes);
        logData.setSource("android");

        Call<Void> log_call = logService.sendLogData(logData);
        log_call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    private void initialize(String url, String token) throws Exception {
        this.logAPI = url;
        this.logAPIToken = token;
        logService = getLogService();
    }

    private String getLogAPIToken(){
        return this.logAPIToken;
    }

    private String getLogAPI(){
        return this.logAPI;
    }

    private LogService getLogService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                //Turning off debugging for now
                //.addInterceptor(interceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder ongoing = chain.request().newBuilder();
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("x-api-key", getLogAPIToken());
                        return chain.proceed(ongoing.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(getLogAPI())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(LogService.class);
    }

    public void setDebug(boolean enabled) {
        isDebugging = enabled;
    }

    public void setCurrentUsername(String username) {
        if (username == null && defaultProps.containsKey(Keys.USERNAME)) {
            defaultProps.remove(Keys.USERNAME);
            return;
        }
        if (username != null) {
            defaultProps.put(Keys.USERNAME, username);
        }
    }

    private interface LogService {

        @POST("log")
        Call<Void> sendLogData(@Body LogData logData);
    }

    static private class LogData {

        enum Level {
            @SerializedName("event")
            EVENT(1),

            @SerializedName("info")
            INFO(2),

            @SerializedName("warn")
            WARN(3),

            @SerializedName("error")
            ERROR(4),

            @SerializedName("fatal")
            FATAL(5),

            @SerializedName("debug")
            DEBUG(6);

            private final int value;

            Level(int i) {
                value = i;
            }

            public int getValue() {
                return value;
            }
        }

        private Level level;
        private String source;
        private String message;
        private Map<String, String> attributes;

        public LogData(Level level, String message){
            this.message = message;
        }


        public LogData(Level level, String message, Map<String, String> attributes){
            this.level = level;
            this.message = message;
            this.attributes = attributes;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public Level getLevel() {
            return level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

}
