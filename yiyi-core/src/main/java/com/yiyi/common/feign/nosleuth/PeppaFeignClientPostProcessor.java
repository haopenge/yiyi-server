package com.yiyi.common.feign.nosleuth;

import com.yiyi.common.feign.AbstractyiyiPrometheusFeignClientPostProcessor;
import com.yiyi.common.feign.yiyiPrometheusFeignContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.openfeign.FeignContext;


public class yiyiFeignClientPostProcessor
        extends AbstractyiyiPrometheusFeignClientPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(yiyiFeignClientPostProcessor.class);

    yiyiFeignClientPostProcessor(BeanFactory beanFactory) {
        super(beanFactory);
        log.info("非zipkin环境 yiyiFeignClientPostProcessor 初始化ok");
    }


    public Object getBean(Object bean) {
        return (bean instanceof FeignContext && !(bean instanceof yiyiPrometheusFeignContext)) ? new yiyiPrometheusFeignContext(
                myFeignObjectWrapper(), (FeignContext) bean) : bean;
    }


    private yiyiPrometheusFeignObjectWrapper myFeignObjectWrapper() {
        return new yiyiPrometheusFeignObjectWrapper(this.beanFactory);
    }
}
