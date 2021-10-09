package com.yiyi.common.feign.sleuth;

import com.yiyi.common.feign.AbstractyiyiPrometheusFeignClientPostProcessor;
import com.yiyi.common.feign.yiyiPrometheusFeignContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.openfeign.FeignContext;


final class yiyiTraceFeignClientPostProcessor
        extends AbstractyiyiPrometheusFeignClientPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(yiyiTraceFeignClientPostProcessor.class);

    yiyiTraceFeignClientPostProcessor(BeanFactory beanFactory) {
        super(beanFactory);
        log.info("zipkin环境 yiyiTraceFeignClientPostProcessor 初始化ok");
    }


    public Object getBean(Object bean) {
        return (bean instanceof FeignContext && !(bean instanceof yiyiPrometheusFeignContext)) ? new yiyiPrometheusFeignContext(
                myFeignObjectWrapper(), (FeignContext) bean) : bean;
    }

    private yiyiTracePrometheusFeignObjectWrapper myFeignObjectWrapper() {
        return new yiyiTracePrometheusFeignObjectWrapper(this.beanFactory);
    }
}
