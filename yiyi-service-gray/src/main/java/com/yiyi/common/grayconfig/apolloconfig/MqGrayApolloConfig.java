package com.yiyi.common.grayconfig.apolloconfig;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.yiyi.common.constant.ConstantProperties;
import com.yiyi.common.grayconfig.ThreadAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Configuration
@ConditionalOnProperty(prefix = "yiyi", name = {"gray"}, havingValue = "true")
public class MqGrayApolloConfig {

    private Logger log = LoggerFactory.getLogger(MqGrayApolloConfig.class);
    private static String forcePodenv = "";

    private static HashMap<String, String> MQ_GRAY_CONFIG = null;

    private static HashMap<String, String> WX_GRAY_CONFIG = null;

    @PostConstruct
    private void init() {
        MQ_GRAY_CONFIG = freshConfig("mqconsumer.gray");
        WX_GRAY_CONFIG = freshConfig("wxlistener.gray");
    }

    private HashMap freshConfig(String graykey) {
        HashMap<String, String> newmap = new HashMap<>();
        Config config = ConfigService.getAppConfig();
        String[] values = config.getArrayProperty(graykey, ",", null);
        if (values != null) {
            for (String value : values) {
                try {
                    int pos = value.indexOf(":");
                    String k = value.substring(0, pos);
                    String v = value.substring(pos + 1);
                    newmap.put(k, v);
                } catch (Exception e) {
                    log.error("解析mqconsumer灰度设置出错:{}", value, e);
                }
            }
        }
        return newmap;
    }

    @ApolloConfigChangeListener(interestedKeys = {"mqconsumer.gray"})
    private void configChangeListter(ConfigChangeEvent changeEvent) {
        MQ_GRAY_CONFIG = checkConfig(changeEvent, "mqconsumer.gray");
    }

    @ApolloConfigChangeListener(interestedKeys = {"wxlistener.gray"})
    private void configChangeListter2(ConfigChangeEvent changeEvent) {
        WX_GRAY_CONFIG = checkConfig(changeEvent, "wxlistener.gray");
    }

    @ApolloConfigChangeListener(interestedKeys = {"podenvforce.gray"})
    private void configChangeListter3(ConfigChangeEvent changeEvent) {
        log.info("**************Apollo动态修改配置**************");
        for (String changedKey : changeEvent.changedKeys()) {
            log.info("changedKey :{}", changedKey);
            String value = changeEvent.getChange(changedKey).getNewValue();
            log.info("changedValue :{}", value);
            if (changedKey.equals("podenvforce.gray")) {
                forcePodenv = (value == null) ? "" : value;
                log.info("强制灰度环境：{}", forcePodenv);
                return;
            }
        }
        forcePodenv = "";
        log.info("强制灰度环境：{}", forcePodenv);
    }

    private HashMap checkConfig(ConfigChangeEvent changeEvent, String key) {
        log.info("**************Apollo动态修改配置**************");
        for (String changedKey : changeEvent.changedKeys()) {
            log.info("changedKey :{}", changedKey);
            String value = changeEvent.getChange(changedKey).getNewValue();
            log.info("changedValue :{}", value);
            if (changedKey.equals(key)) {
                return freshConfig(key);
            }
        }
        return new HashMap<>();
    }


    public static String getTopicPodEnv(String topic) {
        if (MQ_GRAY_CONFIG == null) {
            return null;
        }
        return MQ_GRAY_CONFIG.get(topic);
    }

    public static String getTopicGroupPodEnv(String topicgroup) {
        if (MQ_GRAY_CONFIG == null) {
            return null;
        }
        return MQ_GRAY_CONFIG.get(topicgroup);
    }

    public static String tryForcePodEnv() {
        if (forcePodenv != null && forcePodenv.trim().length() > 0) {
            ThreadAttributes.setThreadAttribute(ConstantProperties.INDEPENDENT_ENV_HEAD_KEY, forcePodenv);
        }
        return forcePodenv;
    }
}