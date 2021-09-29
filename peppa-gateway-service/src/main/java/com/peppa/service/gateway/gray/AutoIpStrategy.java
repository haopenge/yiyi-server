package com.peppa.service.gateway.gray;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.peppa.service.gateway.gray.factory.AnnoStrategy;
import com.peppa.service.gateway.utils.ReactiveRequestUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;


@AnnoStrategy
public class AutoIpStrategy extends StrategyAbstract {
    public AutoIpStrategy() {
        setName("A_IP_");
        setOrder(6);
    }


    public Server getServer(ILoadBalancer balancer, ServerHttpRequest request) {
        String ip = ReactiveRequestUtils.getRealIp(request);
        if (ip == null || ip.equals("")) {
            this.logger.info("HTTP_X_FORWARDED_FOR threadlocal ip:---null!!!");
            return null;
        }
        this.logger.info("HTTP_X_FORWARDED_FOR threadlocal ip:---{}", ip);
        return getIpSameServer(ip, balancer);
    }


    public String getName() {
        return this.name;
    }
}

