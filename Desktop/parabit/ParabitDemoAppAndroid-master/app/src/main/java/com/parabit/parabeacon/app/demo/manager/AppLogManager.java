package com.parabit.parabeacon.app.demo.manager;

import com.parabit.parabeacon.app.demo.log.AppLogAppender;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

public class AppLogManager {

    private org.slf4j.Logger log;

    private static AppLogManager instance;

    private AppLogManager() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setContext(lc);
        ple.start();

        AppLogAppender appAppender = AppLogAppender.getInstance();
        appAppender.setContext(lc);
        appAppender.start();

        log = getLogger("MMRDemoAppLog");
    }

    public static org.slf4j.Logger getLogger() {
        if (instance == null) {
            instance = new AppLogManager();
        }

        return instance.log;
    }

    private Logger getLogger(String logName) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger log = lc.getLogger(logName);
        log.setAdditive(true);
        log.setLevel(Level.ALL);
        log.addAppender(AppLogAppender.getInstance());
        return log;
    }
}
