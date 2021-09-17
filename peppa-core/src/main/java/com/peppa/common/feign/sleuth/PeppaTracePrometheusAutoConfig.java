package com.peppa.common.feign.sleuth;

import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceFeignClientAutoConfiguration;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceLoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(value = {"spring.sleuth.feign.enabled"}, matchIfMissing = true)
@ConditionalOnClass({Client.class, FeignContext.class, TraceLoadBalancerFeignClient.class})
@AutoConfigureAfter({TraceFeignClientAutoConfiguration.class})
public class PeppaTracePrometheusAutoConfig {
    private static final Logger log = LoggerFactory.getLogger(PeppaTracePrometheusAutoConfig.class);


    @Configuration
    @ConditionalOnProperty(prefix = "peppa", name = {"feigncheckpoint"}, havingValue = "true")
    protected static class PeppaTraceFeignBeanPostProcessorConfiguration {
        @Bean
        static PeppaTraceFeignClientPostProcessor peppaTraceFeignClientPostProcessor(BeanFactory beanFactory) {
            return new PeppaTraceFeignClientPostProcessor(beanFactory);
        }
    }
}

