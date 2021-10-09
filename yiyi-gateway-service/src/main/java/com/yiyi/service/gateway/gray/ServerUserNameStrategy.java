package com.yiyi.service.gateway.gray;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.yiyi.service.gateway.gray.factory.AnnoStrategy;
import org.springframework.http.server.reactive.ServerHttpRequest;


@AnnoStrategy
public class ServerUserNameStrategy
        extends StrategyAbstract {
    private final String header_key = "uname";
    private final String strategy_key = "gray-name";

    public ServerUserNameStrategy() {
        setName("S_REGNA_");
        setOrder(5);
    }


    public Server getServer(ILoadBalancer balancer, ServerHttpRequest request) {
        return getServerByHeader(balancer, "uname", "gray-name", request);
    }


    public String getName() {
        return this.name;
    }
}