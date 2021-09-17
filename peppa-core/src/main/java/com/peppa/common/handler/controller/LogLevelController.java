package com.peppa.common.handler.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogLevelController {
    private static final Logger log = LoggerFactory.getLogger(LogLevelController.class);


    @RequestMapping({"/arc/common/logback"})
    public String logj() {
        log.error("当前级别：error");
        log.warn("当前级别：warn");
        log.info("当前级别：info");
        log.debug("当前级别：debug");
        log.trace("当前级别：trace");
        if (log.isTraceEnabled()) {
            return "trace";
        }
        if (log.isDebugEnabled()) {
            return "debug";
        }
        if (log.isInfoEnabled()) {
            return "info";
        }
        if (log.isWarnEnabled()) {
            return "warn";
        }
        if (log.isErrorEnabled()) {
            return "error";
        }
        return "off";
    }


    @RequestMapping({"/arc/common/loglevel"})
    public String updateLogbackLevel(@RequestParam("level") String level, @RequestParam(value = "packageName", defaultValue = "-1") String packageName) throws Exception {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (packageName.equals("-1")) {

            loggerContext.getLogger("root").setLevel(Level.toLevel(level));
        } else {
            loggerContext.getLogger(packageName).setLevel(Level.valueOf(level));
        }
        return "ok";
    }
}

