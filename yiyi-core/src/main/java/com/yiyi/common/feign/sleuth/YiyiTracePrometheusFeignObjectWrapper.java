package com.yiyi.common.feign.sleuth;

import com.yiyi.common.feign.AbstractYiyiPrometheusFeignObjectWrapper;
import com.yiyi.common.feign.YiyiPrometheusFeignClient;
import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceLoadBalancerFeignClient;


public class YiyiTracePrometheusFeignObjectWrapper extends AbstractYiyiPrometheusFeignObjectWrapper {
    private static final Logger log = LoggerFactory.getLogger(YiyiTracePrometheusFeignObjectWrapper.class);

    YiyiTracePrometheusFeignObjectWrapper(BeanFactory beanFactory) {
        super(beanFactory);
    }


    protected Object wrap(Object bean) {
        try {
            if (bean instanceof Client && !(bean instanceof YiyiPrometheusFeignClient)) {

                if (ribbonPresent && bean instanceof TraceLoadBalancerFeignClient && !(bean instanceof YiyiTracePrometheusLoadBalancerFeignClient)) {
                    Client client = ((TraceLoadBalancerFeignClient) bean).getDelegate();
                    return new YiyiTracePrometheusLoadBalancerFeignClient((Client) (new YiyiTracePrometheusFeignObjectWrapper(this.beanFactory)).wrap(client),
                            factory(), (SpringClientFactory) clientFactory(), this.beanFactory);
                }

                return (ribbonPresent && bean instanceof YiyiTracePrometheusLoadBalancerFeignClient) ? bean : new YiyiPrometheusFeignClient((Client) bean);
            }

            return bean;
        } catch (Throwable e) {
            log.error("feign 埋点失效,请联系架构组:{}", e.toString());
            return bean;
        }
    }
}

