package com.peppa.common.feign.sleuth;

import com.peppa.common.feign.AbstractPeppaPrometheusFeignObjectWrapper;
import com.peppa.common.feign.PeppaPrometheusFeignClient;
import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceLoadBalancerFeignClient;


public class PeppaTracePrometheusFeignObjectWrapper
        extends AbstractPeppaPrometheusFeignObjectWrapper {
    private static final Logger log = LoggerFactory.getLogger(PeppaTracePrometheusFeignObjectWrapper.class);

    PeppaTracePrometheusFeignObjectWrapper(BeanFactory beanFactory) {
        super(beanFactory);
    }


    protected Object wrap(Object bean) {
        try {
            if (bean instanceof Client && !(bean instanceof PeppaPrometheusFeignClient)) {

                if (ribbonPresent && bean instanceof TraceLoadBalancerFeignClient && !(bean instanceof PeppaTracePrometheusLoadBalancerFeignClient)) {
                    Client client = ((TraceLoadBalancerFeignClient) bean).getDelegate();
                    return new PeppaTracePrometheusLoadBalancerFeignClient((Client) (new PeppaTracePrometheusFeignObjectWrapper(this.beanFactory)).wrap(client),
                            factory(), (SpringClientFactory) clientFactory(), this.beanFactory);
                }

                return (ribbonPresent && bean instanceof PeppaTracePrometheusLoadBalancerFeignClient) ? bean : new PeppaPrometheusFeignClient((Client) bean);
            }

            return bean;
        } catch (Throwable e) {
            log.error("feign 埋点失效,请联系架构组:{}", e.toString());
            return bean;
        }
    }
}

