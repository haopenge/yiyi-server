package com.yiyi.filter;


import com.yi.core.ribbon.Constants;
import com.yi.core.ribbon.EnvHolder;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
@ConditionalOnProperty(name = "enable_gray_env")
public class GrayEnvLoadBalancerClientFilter extends LoadBalancerClientFilter {
    public GrayEnvLoadBalancerClientFilter(LoadBalancerClient loadBalancer, LoadBalancerProperties properties) {
        super(loadBalancer, properties);
    }


    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {

        // 获取入口环境，存入threadLocal
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String podEnv = headers.getFirst(Constants.POD_ENV);
        if(StringUtils.isNotEmpty(podEnv)){
            EnvHolder.setEnv(Constants.POD_ENV,podEnv);
        }

        return super.choose(exchange);
    }
}
