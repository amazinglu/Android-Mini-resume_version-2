package com.parabit.parabeacon.app.tech.logging;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.parabit.parabeacon.app.tech.R;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.metrics.MetricsManager;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;

/**
 * Created by williamsnyder on 12/14/17.
 */

public class ApplicationLogger {

    private static ApplicationLogger instance;
    private ParabitLogManager logService;

    private ApplicationLogger(Application context) {
        Fabric.with(context, new Crashlytics());
        MetricsManager.register(context);
        CrashManager.register(context);

        try {
            initializeParabitLogger(context);
        } catch (Exception e) {
            logEvent(Events.LOG_SERVICE_UNAVAILABLE);
        }
    }

    public static void initialize(Application context) {
        if (instance == null) {
            instance = new ApplicationLogger(context);
        }
    }

    public static void setCurrentUsername(String username) {
        if (instance.logService != null){
            instance.logService.setCurrentUsername(username);
        }
    }

    public static void setDebug(boolean enabled) {
        instance.logService.setDebug(enabled);
    }

    private void initializeParabitLogger(Context context) throws Exception {
        if (logService == null) {
            String logURL = context.getString(R.string.parabit_log_api);
            String logKey = context.getString(R.string.parabit_log_key);

            if (logURL == null || logKey == null) {
                return;
            }

            logService = ParabitLogManager.getInstance(logURL,logKey);
        }
    }

    // EVENTS

    public static void logEvent(String event) {
        Answers.getInstance().logCustom(new CustomEvent(event));
        MetricsManager.trackEvent(event);
        if (instance.logService != null) {
            instance.logService.logEvent(event);
        }
    }

    public static void logEvent(String event, Map<String,String> properties) {
        CustomEvent answerEvent = new CustomEvent(event);
        for (String key:properties.keySet()) {
            answerEvent.putCustomAttribute(key,properties.get(key));
        }
        Answers.getInstance().logCustom(answerEvent);
        MetricsManager.trackEvent(event, properties);
        if (instance.logService != null) {
            instance.logService.logEvent(event, properties);
        }
    }

    // INFO

    public static void logInfo(String message) {
        if (instance.logService != null) {
            instance.logService.logInfo(message);
        }
    }

    public static void logInfo(String message, Map<String,String> properties) {
        if (instance.logService != null) {
            instance.logService.logInfo(message, properties);
        }
    }

    // WARN
    public static void logWarn(String message) {
        if (instance.logService != null) {
            instance.logService.logWarn(message);
        }
    }

    public static void logWarn(String message, Map<String,String> properties) {
        if (instance.logService != null) {
            instance.logService.logWarn(message, properties);
        }
    }

    // DEBUG

    public static void logDebug(String message) {
        if (instance.logService != null) {
            instance.logService.logDebug(message);
        }
    }

    public static void logDebug(String message, Map<String,String> properties) {
        if (instance.logService != null) {
            instance.logService.logDebug(message, properties);
        }
    }

    public static void logDebug(String message, Map<String,String> properties, Throwable error) {
        if (instance.logService != null) {
            if (properties == null) {
                properties = new HashMap<>();
            }
            properties.put("errorMessage", error.getLocalizedMessage());
            instance.logService.logDebug(message, properties);
        }
    }

    // ERROR

    public static void logError(String message) {
        if (instance.logService != null) {
            instance.logService.logError(message);
        }
    }

    public static void logError(String message, Map<String,String> properties) {
        if (instance.logService != null) {
            instance.logService.logError(message, properties);
        }
    }

    // FATAL

    public static void logFatal(String message) {
        if (instance.logService != null) {
            instance.logService.logFatal(message);
        }
    }

    public static void logFatal(String message, Map<String,String> properties) {
        if (instance.logService != null) {
            instance.logService.logFatal(message, properties);
        }
    }

    public static void logError(String message, Throwable error) {
        if (instance.logService != null) {
            Map<String, String> properties = new HashMap<>();
            if (error != null) {
                properties.put("errorMessage", error.getLocalizedMessage());
            }
            instance.logService.logError(message, properties);
        }
    }

    public static void logError(String message, Map<String,String> properties, Throwable error) {
        if (instance.logService != null) {
            properties.put("errorMessage", error.getLocalizedMessage());
            instance.logService.logError(message, properties);
        }
    }

}
