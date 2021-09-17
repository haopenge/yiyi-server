package com.husky.intf.mq;

import com.peppa.common.mq.producter.PeppaMqProduceMessage;
import com.peppa.common.mq.producter.PeppaMqProducter;
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
    public PeppaMqProducter createDefaultMQProducer() {
        PeppaMqProducter peppaMqProducter = new PeppaMqProducter();
        peppaMqProducter.setNamesServerAddress(namesServerAddress);
        peppaMqProducter.setProducerGroup(group);
        return peppaMqProducter;
    }

    @Bean
    public PeppaMqProduceMessage peppaMqProduceMessageFactoryBean(@Qualifier("defaultMQProducer") PeppaMqProducter defaultMQProducer) {
        PeppaMqProduceMessage peppaMqProduceMessage = new PeppaMqProduceMessage();
        if(defaultMQProducer != null){
            peppaMqProduceMessage.setDefaultMQProducer(defaultMQProducer.getDefaultMQProducer());
        }
        return peppaMqProduceMessage;
    }
}
