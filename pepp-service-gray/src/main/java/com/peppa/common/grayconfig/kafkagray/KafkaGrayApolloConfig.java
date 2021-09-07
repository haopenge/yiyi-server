package com.peppa.common.grayconfig.kafkagray;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.PeppaGrayPartitionAssignor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;


@Configuration
@ConditionalOnClass({PeppaGrayPartitionAssignor.class})
@ConditionalOnProperty(prefix = "peppa", name = {"gray"}, havingValue = "true")
public class KafkaGrayApolloConfig {
    public static final String MQ_GRAY_KEY = "kafkaconsumer.gray";
    private static final Logger logger = LoggerFactory.getLogger(KafkaGrayApolloConfig.class);


    private static ConcurrentHashMap<String, String> MQ_GRAY_CONFIG = new ConcurrentHashMap<>();

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    public static String getTopicPodEnv(String topic) {
        return MQ_GRAY_CONFIG.get(topic);
    }


    public static String getTopicGroupPodEnv(String topicgroup) {
        return MQ_GRAY_CONFIG.get(topicgroup);
    }

    public static List<String> getFilterConsumers(String topic, String groupid, List<String> consumerIds) {
        String topicAssignEnv = getTopicPodEnv(topic);
        String topicGroupAssignEnv = getTopicGroupPodEnv(topic + "." + groupid);

        String apolloAssignedEvn = (topicGroupAssignEnv == null) ? topicAssignEnv : topicGroupAssignEnv;


        boolean isApolloAssigned = (apolloAssignedEvn != null);
        if (isApolloAssigned) {
            String rule = (topicGroupAssignEnv == null) ? (topic + ":" + topicAssignEnv) : (topic + "." + groupid + ":" + topicGroupAssignEnv);

            logger.info("apollo指定了kafka消费的独立环境规则为 {} ,仅指定独立环境【{}】的kafka消费者参与消费", rule, apolloAssignedEvn);
        } else {
            logger.info("apollo没有指定【{}】kafka消费的独立环境规则，所有独立环境的kafka消费者不参与消费！！", topic);
        }
        logger.info("{}.{}原始列表:{}", new Object[]{topic, groupid, consumerIds});
        ArrayList<String> filterArray = new ArrayList<>();
        for (String consumerId : consumerIds) {
            String objEvn = KafkaAspect.getPodenvFromConsumerId(consumerId);
            if (isApolloAssigned) {
                if (objEvn == null) {
                    continue;
                }
                if (objEvn.equals(topicAssignEnv) || objEvn.equals(topicGroupAssignEnv))
                    filterArray.add(consumerId);
                continue;
            }
            if (objEvn.equals("qa") || objEvn.equals("dev") || objEvn.equals("sim")) {
                filterArray.add(consumerId);
            }
        }

        logger.info("{}.{}过滤后列表:{}", new Object[]{topic, groupid, filterArray});
        if (filterArray.size() == 0 && !isApolloAssigned) {
            logger.info("{}.{}没有公共服务消费不进行过滤，返回:{}", new Object[]{topic, groupid, consumerIds});
            return consumerIds;
        }
        return filterArray;
    }

    @PostConstruct
    private void init() {
        freshConfig();
    }

    private void freshConfig() {
        ConcurrentHashMap<String, String> newmap = new ConcurrentHashMap<>();
        Config config = ConfigService.getAppConfig();
        String[] values = config.getArrayProperty("kafkaconsumer.gray", ",", null);
        if (values != null) {
            for (String value : values) {
                try {
                    int pos = value.indexOf(":");
                    String k = value.substring(0, pos);
                    String v = value.substring(pos + 1);
                    newmap.put(k, v);
                } catch (Exception e) {
                    logger.error("解析{}灰度设置出错:{}", new Object[]{"kafkaconsumer.gray", value, e});
                }
            }
        }
        MQ_GRAY_CONFIG = newmap;
    }

    @ApolloConfigChangeListener(interestedKeys = {"kafkaconsumer.gray"})
    private void configChangeListener(ConfigChangeEvent changeEvent) {
        logger.info("**************Apollo动态修改配置**************");
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
            if (changedKey.equals("kafkaconsumer.gray")) {
                freshConfig();
            }
        }
        Collection<MessageListenerContainer> set = this.registry.getListenerContainers();
        for (MessageListenerContainer listenerContainer : set) {
            listenerContainer.stop();
            listenerContainer.start();
        }
    }
}
