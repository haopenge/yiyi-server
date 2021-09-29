package com.peppa.service.gateway.apollo;

import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class GatewayPropertiesRefresher implements ApplicationContextAware, ApplicationEventPublisherAware {
    private static final Logger logger = LoggerFactory.getLogger(GatewayPropertiesRefresher.class);

    private ApplicationContext applicationContext;

    private ApplicationEventPublisher publisher;

    @Autowired
    private GatewayProperties gatewayProperties;


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @ApolloConfigChangeListener
    public void onChange(ConfigChangeEvent changeEvent) {
        refreshGatewayProperties(changeEvent);
    }


    private void refreshGatewayProperties(ConfigChangeEvent changeEvent) {
        logger.info("Refreshing GatewayProperties!");

        preDestroyGatewayProperties(changeEvent);

        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));

        refreshGatewayRouteDefinition();
        logger.info("GatewayProperties refreshed!");
    }


    private synchronized void preDestroyGatewayProperties(ConfigChangeEvent changeEvent) {
        logger.info("Pre Destroy GatewayProperties!");

        boolean needClearRoutes = checkNeedClear(changeEvent, "spring\\.cloud\\.gateway\\.routes\\[\\d+\\]\\.id", this.gatewayProperties.getRoutes().size());
        if (needClearRoutes) {
            this.gatewayProperties.setRoutes(new ArrayList());
        }

        boolean needClearDefaultFilters = checkNeedClear(changeEvent, "spring\\.cloud\\.gateway\\.default-filters\\[\\d+\\]\\.name", this.gatewayProperties.getDefaultFilters().size());
        if (needClearDefaultFilters) {
            this.gatewayProperties.setRoutes(new ArrayList());
        }
        logger.info("Pre Destroy GatewayProperties finished!");
    }

    private void refreshGatewayRouteDefinition() {
        logger.info("Refreshing Gateway RouteDefinition!");
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        logger.info("Gateway RouteDefinition refreshed!");
    }


    private boolean checkNeedClear(ConfigChangeEvent changeEvent, String pattern, int existSize) {
        return changeEvent.changedKeys().stream()
                .filter(key -> key.matches(pattern))
                .filter(key -> {
                    ConfigChange change = changeEvent.getChange(key);
                    return PropertyChangeType.DELETED.equals(change.getChangeType());
                })
                .count() == existSize;
    }
}

