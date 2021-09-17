package com.peppa.common.handler.apollo;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class RefreshConfig
        implements ApplicationContextAware {
    ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @ApolloConfigChangeListener({"application", "t1.springcloud", "t1.la-framework"})
    public void onChange(ConfigChangeEvent changeEvent) {
        refresh(changeEvent);
    }

    public void refresh(ConfigChangeEvent changeEvent) {
        this.applicationContext.publishEvent((ApplicationEvent) new EnvironmentChangeEvent(changeEvent.changedKeys()));
    }
}
