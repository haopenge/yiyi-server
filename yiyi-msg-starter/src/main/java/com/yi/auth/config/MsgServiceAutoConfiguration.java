package com.yi.auth.config;


import com.yi.auth.service.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MsgProperties.class)
@ConditionalOnProperty(prefix = "msg",value = "enable")
public class MsgServiceAutoConfiguration {

    @Autowired
    private MsgProperties msgProperties;

    @Bean
    @ConditionalOnMissingBean(MsgService.class)
    public MsgService msgService(){
        return new MsgService(msgProperties);
    }
}
