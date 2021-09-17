package com.peppa.common.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;


public abstract class AbstractPeppaPrometheusFeignClientPostProcessor
        implements MergedBeanDefinitionPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(AbstractPeppaPrometheusFeignClientPostProcessor.class);
    public final BeanFactory beanFactory;

    public AbstractPeppaPrometheusFeignClientPostProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return getBean(bean);
    }

    public void postProcessMergedBeanDefinition(RootBeanDefinition var1, Class<?> var2, String var3) {
    }

    public abstract Object getBean(Object paramObject);
}


