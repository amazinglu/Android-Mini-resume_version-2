package com.parabit.parabeacon.app.demo.log;

import android.os.Build;

import com.google.common.base.Splitter;
import com.parabit.parabeacon.app.demo.BuildConfig;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.logz.sender.LogzioSender;
import io.logz.sender.SenderStatusReporter;
import io.logz.sender.com.google.gson.JsonObject;
import io.logz.sender.exceptions.LogzioParameterErrorException;

public class LogzioLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String TIMESTAMP = "@timestamp";
    private static final String LOGLEVEL = "loglevel";
    private static final String MARKER = "marker";
    private static final String MESSAGE = "message";
    private static final String LOGGER = "logger";
    private static final String LINE = "line";
    private static final String THREAD = "thread";
    private static final String EXCEPTION = "exception";

    private static final Set<String> reservedFields =  new HashSet<>(Arrays.asList(new String[] {TIMESTAMP,LOGLEVEL, MARKER, MESSAGE,LOGGER,THREAD,EXCEPTION}));

    private LogzioSender logzioSender;
    private ThrowableProxyConverter throwableProxyConverter;
    private LineOfCallerConverter lineOfCallerConverter;
    private Map<String, String> additionalFieldsMap = new HashMap<>();

    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    // User controlled variables
    private String logzioToken;
    private String logzioType = "json";
    private int drainTimeoutSec = 5;
    private int fileSystemFullPercentThreshold = 98;
    private String bufferDir;
    private String logzioUrl;
    private int connectTimeout = 10 * 1000;
    private int socketTimeout = 10 * 1000;
    private boolean debug = false;
    private boolean addHostname = false;
    private boolean line = false;
    private int gcPersistedQueueFilesIntervalSeconds = 30;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public LogzioLogbackAppender() {
        super();
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        additionalFieldsMap.put("os.type", "android");
        additionalFieldsMap.put("os.version", sdkVersion + "_" + release);
        additionalFieldsMap.put("environment", BuildConfig.BUILD_TYPE);
    }

    public void setToken(String logzioToken) {
        this.logzioToken = getValueFromSystemEnvironmentIfNeeded(logzioToken);
    }

    public void setLogzioType(String logzioType) {
        this.logzioType = logzioType;
    }

    public void setDrainTimeoutSec(int drainTimeoutSec) {
        // Basic protection from running negative or zero timeout
        if (drainTimeoutSec < 1) {
            this.drainTimeoutSec = 1;
            addInfo("Got unsupported drain timeout " + drainTimeoutSec + ". The timeout must be number greater then 1. I have set to 1 as fallback.");
        } else {
            this.drainTimeoutSec = drainTimeoutSec;
        }
    }

    public void setFileSystemFullPercentThreshold(int fileSystemFullPercentThreshold) {
        this.fileSystemFullPercentThreshold = fileSystemFullPercentThreshold;
    }

    public void setBufferDir(String bufferDir) {
        this.bufferDir = bufferDir;
    }

    public void setLogzioUrl(String logzioUrl) {
        this.logzioUrl = getValueFromSystemEnvironmentIfNeeded(logzioUrl);
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    public void setAdditionalFields(String additionalFields) {
       if (additionalFields != null) {
           Map<String, String> addFieldMap = Splitter.on(';').omitEmptyStrings().withKeyValueSeparator('=').split(additionalFields);
           for (Map.Entry<String, String> entry : addFieldMap.entrySet()) {
               String k = entry.getKey();
               String v = entry.getValue();
               if (reservedFields.contains(k)) {
                   addWarn("The field name '" + k + "' defined in additionalFields configuration can't be used since it's a reserved field name. This field will not be added to the outgoing log messages");
               } else {
                   String value = getValueFromSystemEnvironmentIfNeeded(v);
                   if (value != null) {
                       additionalFieldsMap.put(k, value);
                   }
               }
           }
           addInfo("The additional fields that would be added: " + additionalFieldsMap.toString());
       }
    }

    public boolean isAddHostname() {
        return addHostname;
    }

    public void setAddHostname(boolean addHostname) {
        this.addHostname = addHostname;
    }

    public boolean isLine() {
        return line;
    }

    public void setLine(boolean line) {
        this.line = line;
    }

    public void setGcPersistedQueueFilesIntervalSeconds(int gcPersistedQueueFilesIntervalSeconds) {
        this.gcPersistedQueueFilesIntervalSeconds = gcPersistedQueueFilesIntervalSeconds;
    }

    @Override
    public void start() {
        if (logzioToken == null) {
            addError("Logz.io Token is missing! Bailing out..");
            return;
        }
        if (!(fileSystemFullPercentThreshold >= 1 && fileSystemFullPercentThreshold <= 100)) {
            if (fileSystemFullPercentThreshold != -1) {
                addError("fileSystemFullPercentThreshold should be a number between 1 and 100, or -1");
                return;
            }
        }
        try {
            if (addHostname) {
                String hostname = InetAddress.getLocalHost().getHostName();
                additionalFieldsMap.put("hostname", hostname);
            }
        } catch (UnknownHostException e) {
            addWarn("The configuration addHostName was specified but the host could not be resolved, thus the field 'hostname' will not be added", e);
        }
        if (bufferDir != null) {
            bufferDir += File.separator + logzioType;
            File bufferFile = new File(bufferDir);
            if (bufferFile.exists()) {
                if (!bufferFile.canWrite()) {
                    addError("We cant write to your bufferDir location: "+bufferFile.getAbsolutePath());
                    return;
                }
            } else {
                if (!bufferFile.mkdirs()) {
                    addError("We cant create your bufferDir location: "+bufferFile.getAbsolutePath());
                    return;
                }
            }
        } else {
            bufferDir = System.getProperty("java.io.tmpdir") + File.separator+"logzio-logback-buffer"+File.separator + logzioType;
        }
        File bufferDirFile = new File(bufferDir,"logzio-logback-appender");
        try {
            SenderStatusReporter reporter = new StatusReporter();
            logzioSender = LogzioSender.getOrCreateSenderByType(logzioToken, logzioType, drainTimeoutSec, fileSystemFullPercentThreshold,
                    bufferDirFile, logzioUrl, socketTimeout, connectTimeout, debug,
                    reporter, scheduler, gcPersistedQueueFilesIntervalSeconds);
            logzioSender.start();
        } catch (LogzioParameterErrorException e) {
            addError("Some of the configuration parameters of logz.io is wrong: "+e.getMessage(), e);
            return;
        }
        throwableProxyConverter = new ThrowableProxyConverter();
        lineOfCallerConverter = new LineOfCallerConverter();
        throwableProxyConverter.setOptionList(Arrays.asList("full"));
        throwableProxyConverter.start();
        super.start();
    }

    @Override
    public void stop() {
        if (logzioSender != null) logzioSender.stop();
        if ( throwableProxyConverter != null ) throwableProxyConverter.stop();
        super.stop();
    }

    private String getValueFromSystemEnvironmentIfNeeded(String value) {
        if (value != null && value.startsWith("$")) {
            return System.getenv(value.replace("$", ""));
        }
        return value;
    }

    private JsonObject formatMessageAsJson(ILoggingEvent loggingEvent) {
        JsonObject logMessage = new JsonObject();

        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();
        // Adding MDC first, as I dont want it to collide with any one of the following fields
        if (mdcPropertyMap != null) {
            for (Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
                logMessage.addProperty(entry.getKey(), entry.getValue());
            }
        }

        long logTime = loggingEvent.getTimeStamp();
        Date logDate = new Date(logTime);
        String logTimeString = logDateFormat.format(logDate);
        logMessage.addProperty(TIMESTAMP, logTimeString);
        logMessage.addProperty(LOGLEVEL,loggingEvent.getLevel().levelStr);

        if (loggingEvent.getMarker() != null) {
            logMessage.addProperty(MARKER, loggingEvent.getMarker().toString());
        }

        logMessage.addProperty(MESSAGE, loggingEvent.getFormattedMessage());
        logMessage.addProperty(LOGGER, loggingEvent.getLoggerName());
        logMessage.addProperty(THREAD, loggingEvent.getThreadName());
        if (line) {
            logMessage.addProperty(LINE, lineOfCallerConverter.convert(loggingEvent));
        }

        if (loggingEvent.getThrowableProxy() != null) {
            logMessage.addProperty(EXCEPTION, throwableProxyConverter.convert(loggingEvent));
        }

        if (additionalFieldsMap != null) {
            for (Map.Entry<String, String> entry : additionalFieldsMap.entrySet()) {
                logMessage.addProperty(entry.getKey(), entry.getValue());
            }
        }

        Object[] arguments = loggingEvent.getArgumentArray();
        if (arguments != null) {
            for (Object arg: arguments) {
                if (arg instanceof Map) {
                    Map props = (Map) arg;
                    for (Object key : props.keySet()) {
                        logMessage.addProperty(key.toString(), props.get(key).toString());
                    }
                }
            }
        }

        return logMessage;
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        if (!loggingEvent.getLoggerName().contains("io.logz.sender")) {
            logzioSender.send(formatMessageAsJson(loggingEvent));
        }
    }

    private class StatusReporter implements SenderStatusReporter {

        @Override
        public void error(String msg) {
            addError(msg);
        }

        @Override
        public void error(String msg, Throwable e) {
            addError(msg, e);
        }

        @Override
        public void warning(String msg) {
            addWarn(msg);
        }

        @Override
        public void warning(String msg, Throwable e) {
            addWarn(msg, e);
        }

        @Override
        public void info(String msg) {
            addInfo(msg);
        }

        @Override
        public void info(String msg, Throwable e) {
            addInfo(msg,e);
        }
    }

}