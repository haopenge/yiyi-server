package com.peppa.common.feign;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.util.ClassUtils;


public abstract class AbstractPeppaPrometheusFeignObjectWrapper {
    public static final boolean ribbonPresent = (
            ClassUtils.isPresent("org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient", (ClassLoader) null) && ClassUtils.isPresent("org.springframework.cloud.netflix.ribbon.SpringClientFactory", (ClassLoader) null));

    public final BeanFactory beanFactory;
    private CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;
    private Object springClientFactory;

    public AbstractPeppaPrometheusFeignObjectWrapper(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected abstract Object wrap(Object paramObject);

    protected CachingSpringLoadBalancerFactory factory() {
        if (this.cachingSpringLoadBalancerFactory == null) {
            this.cachingSpringLoadBalancerFactory = (CachingSpringLoadBalancerFactory) this.beanFactory.getBean(CachingSpringLoadBalancerFactory.class);
        }


        return this.cachingSpringLoadBalancerFactory;
    }

    protected Object clientFactory() {
        if (this.springClientFactory == null) {
            this.springClientFactory = this.beanFactory.getBean(SpringClientFactory.class);
        }

        return this.springClientFactory;
    }
}
