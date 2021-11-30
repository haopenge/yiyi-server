package com.yiyi.common.feign.sleuth;

import com.yiyi.common.feign.AbstractYiyiPrometheusFeignClientPostProcessor;
import com.yiyi.common.feign.YiyiPrometheusFeignContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.openfeign.FeignContext;


final class YiyiTraceFeignClientPostProcessor extends AbstractYiyiPrometheusFeignClientPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(YiyiTraceFeignClientPostProcessor.class);

    YiyiTraceFeignClientPostProcessor(BeanFactory beanFactory) {
        super(beanFactory);
        log.info("zipkin环境 yiyiTraceFeignClientPostProcessor 初始化ok");
    }


    public Object getBean(Object bean) {
        return (bean instanceof FeignContext && !(bean instanceof YiyiPrometheusFeignContext)) ? new YiyiPrometheusFeignContext(
                myFeignObjectWrapper(), (FeignContext) bean) : bean;
    }

    private YiyiTracePrometheusFeignObjectWrapper myFeignObjectWrapper() {
        return new YiyiTracePrometheusFeignObjectWrapper(this.beanFactory);
    }
}
