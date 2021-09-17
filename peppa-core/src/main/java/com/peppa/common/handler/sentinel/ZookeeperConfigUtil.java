package com.peppa.common.handler.sentinel;

import org.apache.commons.lang.StringUtils;


public class ZookeeperConfigUtil {
    public static final String SENTIENL_ROOT = "/sentinel";
    public static final String RULE_ROOT_PATH = "/rule_config_";
    public static final String ID_GEN_PATH = "/rule_id";
    public static final int RETRY_TIMES = 3;
    public static final int SLEEP_TIME = 1000;

    public static String getPath(String appName, RuleTypeEnum ruleTypeEnum) {
        StringBuilder stringBuilder = new StringBuilder("/sentinel");
        stringBuilder.append("/rule_config_");
        stringBuilder.append(ruleTypeEnum.getString());
        if (StringUtils.isBlank(appName)) {
            return stringBuilder.toString();
        }
        if (appName.startsWith("/")) {
            stringBuilder.append(appName);
        } else {
            stringBuilder.append("/")
                    .append(appName);
        }
        return stringBuilder.toString();
    }

    public static String getIdGenPath() {
        return "/sentinel/rule_id";
    }

    public static String getRootPath() {
        return "/sentinel";
    }
}


