package com.husky.mq;

import com.yiyi.common.mq.producter.yiyiMqProduceMessage;
import com.yiyi.common.mq.producter.yiyiMqProducter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rocketMq 配置
 */
@Slf4j
@Data
@Configuration
public class RocketMqConfig {

    @Value("${app.id}")
    private String group;

    @Value("${mq.namesServerAddress:}")
    private String namesServerAddress;

    @Bean(name = "defaultMQProducer", initMethod = "initDefaultMQProducer", destroyMethod = "close")
    public yiyiMqProducter createDefaultMQProducer() {
        yiyiMqProducter yiyiMqProducter = new yiyiMqProducter();
        yiyiMqProducter.setNamesServerAddress(namesServerAddress);
        yiyiMqProducter.setProducerGroup(group);
        return yiyiMqProducter;
    }

    @Bean
    public yiyiMqProduceMessage yiyiMqProduceMessageFactoryBean(@Qualifier("defaultMQProducer") yiyiMqProducter defaultMQProducer) {
        yiyiMqProduceMessage yiyiMqProduceMessage = new yiyiMqProduceMessage();
        if(defaultMQProducer != null){
            yiyiMqProduceMessage.setDefaultMQProducer(defaultMQProducer.getDefaultMQProducer());
        }
        return yiyiMqProduceMessage;
    }
}
