package com.yiyi.common.feign.nosleuth;

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
public class YiyiPrometheusAutoConfig {
    private static final Logger log = LoggerFactory.getLogger(YiyiPrometheusAutoConfig.class);

    @Configuration
    @ConditionalOnProperty(prefix = "yiyi", name = {"feigncheckpoint"}, havingValue = "true")
    protected static class yiyiFeignBeanPostProcessorConfiguration {
        @Bean
        static YiyiFeignClientPostProcessor yiyiFeignClientPostProcessor(BeanFactory beanFactory) {
            return new YiyiFeignClientPostProcessor(beanFactory);
        }
    }
}
