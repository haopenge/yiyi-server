package com.yiyi.common.feign.nosleuth;

import com.yiyi.common.feign.AbstractYiyiPrometheusFeignClientPostProcessor;
import com.yiyi.common.feign.YiyiPrometheusFeignContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.openfeign.FeignContext;


public class YiyiFeignClientPostProcessor extends AbstractYiyiPrometheusFeignClientPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(YiyiFeignClientPostProcessor.class);

    YiyiFeignClientPostProcessor(BeanFactory beanFactory) {
        super(beanFactory);
        log.info("非zipkin环境 yiyiFeignClientPostProcessor 初始化ok");
    }


    public Object getBean(Object bean) {
        return (bean instanceof FeignContext && !(bean instanceof YiyiPrometheusFeignContext)) ? new YiyiPrometheusFeignContext(
                myFeignObjectWrapper(), (FeignContext) bean) : bean;
    }


    private YiyiPrometheusFeignObjectWrapper myFeignObjectWrapper() {
        return new YiyiPrometheusFeignObjectWrapper(this.beanFactory);
    }
}
