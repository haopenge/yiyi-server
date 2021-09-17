package com.peppa.common.feign.nosleuth;

import com.peppa.common.feign.AbstractPeppaPrometheusFeignObjectWrapper;
import com.peppa.common.feign.PeppaPrometheusFeignClient;
import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;


public class PeppaPrometheusFeignObjectWrapper
        extends AbstractPeppaPrometheusFeignObjectWrapper {
    private static final Logger log = LoggerFactory.getLogger(PeppaPrometheusFeignObjectWrapper.class);

    PeppaPrometheusFeignObjectWrapper(BeanFactory beanFactory) {
        super(beanFactory);
    }


    protected Object wrap(Object bean) {
        try {
            if (bean instanceof Client && !(bean instanceof PeppaPrometheusFeignClient)) {

                if (ribbonPresent && bean instanceof LoadBalancerFeignClient && !(bean instanceof PeppaPrometheusLoadBalancerFeignClient)) {
                    Client client = ((LoadBalancerFeignClient) bean).getDelegate();
                    return new PeppaPrometheusLoadBalancerFeignClient((Client) (new PeppaPrometheusFeignObjectWrapper(this.beanFactory)).wrap(client), factory(), (SpringClientFactory)
                            clientFactory(), this.beanFactory);
                }

                return (ribbonPresent && bean instanceof PeppaPrometheusLoadBalancerFeignClient) ? bean : new PeppaPrometheusFeignClient((Client) bean);
            }

            return bean;
        } catch (Throwable e) {
            log.error("feign 埋点失效,请联系架构组:{}", e.toString());
            return bean;
        }
    }
}

