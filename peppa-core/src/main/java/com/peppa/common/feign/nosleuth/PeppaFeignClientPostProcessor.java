package com.peppa.common.feign.nosleuth;

import com.peppa.common.feign.AbstractPeppaPrometheusFeignClientPostProcessor;
import com.peppa.common.feign.PeppaPrometheusFeignContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.openfeign.FeignContext;


public class PeppaFeignClientPostProcessor
        extends AbstractPeppaPrometheusFeignClientPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(PeppaFeignClientPostProcessor.class);

    PeppaFeignClientPostProcessor(BeanFactory beanFactory) {
        super(beanFactory);
        log.info("非zipkin环境 PeppaFeignClientPostProcessor 初始化ok");
    }


    public Object getBean(Object bean) {
        return (bean instanceof FeignContext && !(bean instanceof PeppaPrometheusFeignContext)) ? new PeppaPrometheusFeignContext(
                myFeignObjectWrapper(), (FeignContext) bean) : bean;
    }


    private PeppaPrometheusFeignObjectWrapper myFeignObjectWrapper() {
        return new PeppaPrometheusFeignObjectWrapper(this.beanFactory);
    }
}
