package com.peppa.service.gateway.gray.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AnnotationScan {
    @Autowired
    ApplicationContext applicationContext;

    @Bean(name = {"strategyFactroy"}, initMethod = "init")
    StrategyFactory strategyFactroy() {
        return new StrategyFactory(this.applicationContext);
    }
}

