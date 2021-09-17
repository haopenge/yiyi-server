package com.peppa.common.feign.sleuth;

import com.peppa.common.feign.AbstractPeppaPrometheusFeignClientPostProcessor;
import com.peppa.common.feign.PeppaPrometheusFeignContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.openfeign.FeignContext;


final class PeppaTraceFeignClientPostProcessor
        extends AbstractPeppaPrometheusFeignClientPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(PeppaTraceFeignClientPostProcessor.class);

    PeppaTraceFeignClientPostProcessor(BeanFactory beanFactory) {
        super(beanFactory);
        log.info("zipkin环境 PeppaTraceFeignClientPostProcessor 初始化ok");
    }


    public Object getBean(Object bean) {
        return (bean instanceof FeignContext && !(bean instanceof PeppaPrometheusFeignContext)) ? new PeppaPrometheusFeignContext(
                myFeignObjectWrapper(), (FeignContext) bean) : bean;
    }

    private PeppaTracePrometheusFeignObjectWrapper myFeignObjectWrapper() {
        return new PeppaTracePrometheusFeignObjectWrapper(this.beanFactory);
    }
}
