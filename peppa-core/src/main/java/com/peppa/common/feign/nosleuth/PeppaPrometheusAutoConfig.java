package com.peppa.common.feign.nosleuth;

import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnClass({Client.class, FeignContext.class})
@ConditionOnNoSleuth
public class PeppaPrometheusAutoConfig {
    private static final Logger log = LoggerFactory.getLogger(PeppaPrometheusAutoConfig.class);

    @Configuration
    @ConditionalOnProperty(prefix = "peppa", name = {"feigncheckpoint"}, havingValue = "true")
    protected static class PeppaFeignBeanPostProcessorConfiguration {
        @Bean
        static PeppaFeignClientPostProcessor peppaFeignClientPostProcessor(BeanFactory beanFactory) {
            return new PeppaFeignClientPostProcessor(beanFactory);
        }
    }
}
