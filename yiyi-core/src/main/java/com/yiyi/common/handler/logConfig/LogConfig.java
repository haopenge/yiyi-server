package com.yiyi.common.handler.logConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class LogConfig {
    private static final Logger log = LoggerFactory.getLogger(LogConfig.class);

    private static String POD_NAME = "";
    private static String POD_IP = "";
    private static String NODE_NAME = "";
    private static String NODE_IP = "";
    private static String APP_NAME = "";

    static {
        try {
            Map<String, String> map = System.getenv();
            POD_NAME = (map.get("POD_NAME") == null) ? "zzz" : map.get("POD_NAME");
            POD_IP = (map.get("POD_IP") == null) ? "zzz" : map.get("POD_IP");
            NODE_NAME = (map.get("NODE_NAME") == null) ? "zzz" : map.get("NODE_NAME");
            NODE_IP = (map.get("NODE_IP") == null) ? "zzz" : map.get("NODE_IP");
            APP_NAME = (map.get("APP_NAME") == null) ? "zzz" : map.get("APP_NAME");
        } catch (Exception e) {
            log.error("获取环境变量失败,影响logback的文件拼接", e);
        }
    }

    public static String getPodName() {
        return POD_NAME;
    }

    public static String getPodIp() {
        return POD_IP;
    }

    public static String getNodeName() {
        return NODE_NAME;
    }

    public static String getNodeIp() {
        return NODE_IP;
    }

    public static String getAppName() {
        return APP_NAME;
    }
}
