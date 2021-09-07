package com.peppa.common.grayconfig.apolloconfig;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.peppa.common.grayconfig.ThreadAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;


@Configuration
@ConditionalOnProperty(prefix = "peppa", name = {"gray"}, havingValue = "true")
public class MqGrayApolloConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String MQ_GRAY_KEY = "mqconsumer.gray";

    public static final String WX_GRAY_KEY = "wxlistener.gray";
    public static final String FORCE_GRAY_KEY = "podenvforce.gray";
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
                    this.logger.error("解析mqconsumer灰度设置出错:{}", value, e);
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
        this.logger.info("**************Apollo动态修改配置**************");
        for (String changedKey : changeEvent.changedKeys()) {
            this.logger.info("changedKey :{}", changedKey);
            String value = changeEvent.getChange(changedKey).getNewValue();
            this.logger.info("changedValue :{}", value);
            if (changedKey.equals("podenvforce.gray")) {
                forcePodenv = (value == null) ? "" : value;
                this.logger.info("强制灰度环境：{}", forcePodenv);
                return;
            }
        }
        forcePodenv = "";
        this.logger.info("强制灰度环境：{}", forcePodenv);
    }

    private HashMap checkConfig(ConfigChangeEvent changeEvent, String key) {
        this.logger.info("**************Apollo动态修改配置**************");
        for (String changedKey : changeEvent.changedKeys()) {
            this.logger.info("changedKey :{}", changedKey);
            String value = changeEvent.getChange(changedKey).getNewValue();
            this.logger.info("changedValue :{}", value);
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


    public static String getOpenidPodEnv(String openid) {
        if (WX_GRAY_CONFIG == null) {
            return null;
        }
        return WX_GRAY_CONFIG.get(openid);
    }


    public static String invokeOpenidPodEnv(String openid) {
        String podenv = getOpenidPodEnv(openid);
        if (podenv != null && podenv.trim().length() > 0) {
            ThreadAttributes.setThreadAttribute("huohua-".concat("podenv"), podenv);
        }
        return podenv;
    }


    public static String tryForcePodEnv() {
        if (forcePodenv != null && forcePodenv.trim().length() > 0) {
            ThreadAttributes.setThreadAttribute("huohua-".concat("podenv"), forcePodenv);
        }
        return forcePodenv;
    }
}