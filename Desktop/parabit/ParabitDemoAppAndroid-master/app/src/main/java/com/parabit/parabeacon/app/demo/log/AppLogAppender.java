package com.parabit.parabeacon.app.demo.log;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.util.CachingDateFormatter;

public class AppLogAppender extends AppenderBase<ILoggingEvent> {

    private static final String NEW_LOG = "NEW_LOG";
    private static AppLogAppender instance;
    private CachingDateFormatter dateFormatter;

    private List<AppLogListener> listenerList = new ArrayList<>();

    private AppLogAppender() {
        dateFormatter = new CachingDateFormatter("HH:mm:ss,SSS");
    }

    public static AppLogAppender getInstance() {
        if (instance == null) {
            instance = new AppLogAppender();
        }

        return instance;
    }

    public void addLogListener(AppLogListener appLogListener) {
        listenerList.add(appLogListener);
    }

    public void removeLogListener(AppLogListener appLogListener) {
        listenerList.remove(appLogListener);
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String message = eventObject.getMessage();
        String date = dateFormatter.format(eventObject.getTimeStamp());
        String level = eventObject.getLevel().levelStr;
        for (AppLogListener appLogListener: listenerList) {
            appLogListener.onLog("[" + level + " : " + date + "] "+message);
        }
    }


}
