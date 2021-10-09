package com.yiyi.common.feign.sleuth;

import com.yiyi.common.feign.PrometheusFeignCounter;
import feign.Client;
import feign.Request;
import feign.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceLoadBalancerFeignClient;

import java.io.IOException;


public class yiyiTracePrometheusLoadBalancerFeignClient
        extends TraceLoadBalancerFeignClient {
    private static final Log log = LogFactory.getLog(yiyiTracePrometheusLoadBalancerFeignClient.class);
    private final BeanFactory beanFactory;

    public yiyiTracePrometheusLoadBalancerFeignClient(Client delegate, CachingSpringLoadBalancerFactory lbClientFactory, SpringClientFactory clientFactory, BeanFactory beanFactory) {
        super(delegate, lbClientFactory, clientFactory, beanFactory);
        this.beanFactory = beanFactory;
    }

    public Response execute(Request request, Request.Options options) throws IOException {
        Response response = null;
        try {
            response = super.execute(request, options);
            count(request, "" + response.status());
        } catch (Exception exp) {
            if (!(exp instanceof IOException)) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception thrown", exp);
                }
                count(request, "505");
            }
            throw exp;
        } finally {
        }

        return response;
    }

    private void count(Request request, String code) {
        try {
            String uri = request.url();
            String method = request.method();
            PrometheusFeignCounter.count(method, uri, code);
        } catch (Exception e) {
            log.error("feign 计数异常：", e);
        }
    }
}


