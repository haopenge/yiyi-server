package com.husky.mq;

import com.yiyi.producter.YiyiMqProduceMessage;
import com.yiyi.producter.YiyiMqProducter;
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
    public YiyiMqProducter createDefaultMQProducer() {
        YiyiMqProducter mqProducter = new YiyiMqProducter();
        mqProducter.setNamesServerAddress(namesServerAddress);
        mqProducter.setProducerGroup(group);
        return mqProducter;
    }

    @Bean
    public YiyiMqProduceMessage yiyiMqProduceMessageFactoryBean(@Qualifier("defaultMQProducer") YiyiMqProducter defaultMQProducer) {
        YiyiMqProduceMessage mqProduceMessage = new YiyiMqProduceMessage();
        if(defaultMQProducer != null){
            mqProduceMessage.setDefaultMQProducer(defaultMQProducer.getDefaultMQProducer());
        }
        return mqProduceMessage;
    }
}
