package com.peppa.common.grayconfig.Strategy.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Configuration
@ConditionalOnProperty(prefix = "peppa", name = {"gray"}, havingValue = "true")
public class AnnotationScan {
    @Autowired
    ApplicationContext applicationContext;

    @Bean(name = {"strategyFactroy"}, initMethod = "init")
    StrategyFactory strategyFactroy() {
        return new StrategyFactory(this.applicationContext);
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Component
    public static @interface AnnoStrategy {
    }
}