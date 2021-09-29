package com.yiyi.config;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * apollo 刷新 gateway配置不同步 问题解决
 */
@Component
public class GatewayPropertiesRefresher implements ApplicationContextAware, ApplicationEventPublisherAware {
    private static final Logger logger = LoggerFactory.getLogger(GatewayPropertiesRefresher.class);

    private ApplicationContext applicationContext;

    private ApplicationEventPublisher publisher;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @ApolloConfigChangeListener
    public void onChange(ConfigChangeEvent changeEvent) {
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        logger.info("GatewayPropertiesRefresher.onChange apollo GatewayProperties refreshed");
    }
}

