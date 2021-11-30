package com.yiyi.common.feign.nosleuth;

import com.yiyi.common.feign.AbstractYiyiPrometheusFeignObjectWrapper;
import com.yiyi.common.feign.YiyiPrometheusFeignClient;
import feign.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;


public class YiyiPrometheusFeignObjectWrapper extends AbstractYiyiPrometheusFeignObjectWrapper {
    private static final Logger log = LoggerFactory.getLogger(YiyiPrometheusFeignObjectWrapper.class);

    YiyiPrometheusFeignObjectWrapper(BeanFactory beanFactory) {
        super(beanFactory);
    }


    protected Object wrap(Object bean) {
        try {
            if (bean instanceof Client && !(bean instanceof YiyiPrometheusFeignClient)) {

                if (ribbonPresent && bean instanceof LoadBalancerFeignClient && !(bean instanceof YiyiPrometheusLoadBalancerFeignClient)) {
                    Client client = ((LoadBalancerFeignClient) bean).getDelegate();
                    return new YiyiPrometheusLoadBalancerFeignClient((Client) (new YiyiPrometheusFeignObjectWrapper(this.beanFactory)).wrap(client), factory(), (SpringClientFactory)
                            clientFactory(), this.beanFactory);
                }

                return (ribbonPresent && bean instanceof YiyiPrometheusLoadBalancerFeignClient) ? bean : new YiyiPrometheusFeignClient((Client) bean);
            }

            return bean;
        } catch (Throwable e) {
            log.error("feign 埋点失效,请联系架构组:{}", e.toString());
            return bean;
        }
    }
}

