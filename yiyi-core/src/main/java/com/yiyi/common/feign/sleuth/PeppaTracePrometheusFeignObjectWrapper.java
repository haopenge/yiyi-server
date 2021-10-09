package com.yiyi.common.feign.sleuth;

import com.yiyi.common.feign.AbstractyiyiPrometheusFeignObjectWrapper;
import com.yiyi.common.feign.yiyiPrometheusFeignClient;
import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceLoadBalancerFeignClient;


public class yiyiTracePrometheusFeignObjectWrapper
        extends AbstractyiyiPrometheusFeignObjectWrapper {
    private static final Logger log = LoggerFactory.getLogger(yiyiTracePrometheusFeignObjectWrapper.class);

    yiyiTracePrometheusFeignObjectWrapper(BeanFactory beanFactory) {
        super(beanFactory);
    }


    protected Object wrap(Object bean) {
        try {
            if (bean instanceof Client && !(bean instanceof yiyiPrometheusFeignClient)) {

                if (ribbonPresent && bean instanceof TraceLoadBalancerFeignClient && !(bean instanceof yiyiTracePrometheusLoadBalancerFeignClient)) {
                    Client client = ((TraceLoadBalancerFeignClient) bean).getDelegate();
                    return new yiyiTracePrometheusLoadBalancerFeignClient((Client) (new yiyiTracePrometheusFeignObjectWrapper(this.beanFactory)).wrap(client),
                            factory(), (SpringClientFactory) clientFactory(), this.beanFactory);
                }

                return (ribbonPresent && bean instanceof yiyiTracePrometheusLoadBalancerFeignClient) ? bean : new yiyiPrometheusFeignClient((Client) bean);
            }

            return bean;
        } catch (Throwable e) {
            log.error("feign 埋点失效,请联系架构组:{}", e.toString());
            return bean;
        }
    }
}

