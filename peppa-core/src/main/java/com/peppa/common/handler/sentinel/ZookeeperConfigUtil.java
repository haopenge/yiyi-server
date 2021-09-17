package com.peppa.common.handler.sentinel;


import com.ctrip.framework.apollo.core.utils.StringUtils;

public class ZookeeperConfigUtil {
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


