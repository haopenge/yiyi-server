package com.peppa.common.grayconfig.kafkagray;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.PeppaGrayPartitionAssignor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnClass({PeppaGrayPartitionAssignor.class})
@ConditionalOnProperty(prefix = "peppa", name = {"gray"}, havingValue = "true")
public class KafkaPartitionGrayApolloConfig {
    public static final String MQ_GRAY_KEY = "kafkaconsumer.partitiongray";
    private static final Logger logger = LoggerFactory.getLogger(KafkaPartitionGrayApolloConfig.class);


    private static ConcurrentHashMap<String, String> MQ_GRAY_CONFIG = new ConcurrentHashMap<>();


    public static String getTopicPartitionPodEnv(String topicpartition) {
        return MQ_GRAY_CONFIG.get(topicpartition);
    }

    @PostConstruct
    private void init() {
        freshConfig();
    }

    private void freshConfig() {
        ConcurrentHashMap<String, String> newmap = new ConcurrentHashMap<>();
        Config config = ConfigService.getAppConfig();
        String[] values = config.getArrayProperty("kafkaconsumer.partitiongray", ",", null);
        if (values != null) {
            for (String value : values) {
                try {
                    int pos = value.indexOf(":");
                    String key = value.substring(0, pos);
                    int postopic = key.lastIndexOf(".");
                    String topic = key.substring(0, postopic);
                    String k = key.substring(postopic + 1);
                    String[] karr = k.split("_");
                    for (String ks : karr) {
                        String v = value.substring(pos + 1);
                        newmap.put(topic + "." + ks, v);
                    }
                } catch (Exception e) {
                    logger.error("解析{}灰度设置出错:{}", new Object[]{"kafkaconsumer.partitiongray", value, e});
                }
            }
        }
        MQ_GRAY_CONFIG = newmap;
    }

    @ApolloConfigChangeListener(interestedKeys = {"kafkaconsumer.partitiongray"})
    private void configChangeListter(ConfigChangeEvent changeEvent) {
        logger.info("**************Apollo {} 动态修改配置**************", "kafkaconsumer.partitiongray");
        try {
            Class.forName("org.apache.kafka.clients.consumer.PeppaGrayPartitionAssignor");
        } catch (Exception e) {
            logger.error("启用了灰度模式，但是kafka 客户端版本非火花定制版本！请联系架构组解决！");
            return;
        }
        for (String changedKey : changeEvent.changedKeys()) {
            logger.info("changedKey :{}", changedKey);
            String value = changeEvent.getChange(changedKey).getNewValue();
            logger.info("changedValue :{}", value);
            if (changedKey.equals("kafkaconsumer.partitiongray"))
                freshConfig();
        }
    }
}
