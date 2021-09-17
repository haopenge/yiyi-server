package com.peppa.common.handler.logConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;


public class LogbackStartupListener
        extends ContextAwareBase
        implements LoggerContextListener, LifeCycle {
    private boolean started = false;

    public void start() {
        if (this.started) {
            return;
        }
        Context context = getContext();
        context.putProperty("POD_NAME", LogConfig.getPodName());
        context.putProperty("POD_IP", LogConfig.getPodIp());
        context.putProperty("NODE_NAME", LogConfig.getNodeName());
        context.putProperty("NODE_IP", LogConfig.getNodeIp());
        context.putProperty("APP_NAME", LogConfig.getAppName());
        this.started = true;
    }


    public void onReset(LoggerContext context) {
    }


    public void onStart(LoggerContext context) {
    }


    public void onStop(LoggerContext context) {
    }


    public void onLevelChange(Logger logger, Level level) {
    }


    public boolean isResetResistant() {
        return true;
    }


    public void stop() {
    }


    public boolean isStarted() {
        return this.started;
    }
}

