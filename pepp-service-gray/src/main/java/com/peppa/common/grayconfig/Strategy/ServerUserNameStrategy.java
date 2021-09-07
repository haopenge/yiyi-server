package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.peppa.common.grayconfig.Strategy.factory.AnnotationScan.AnnoStrategy;


@AnnoStrategy
public class ServerUserNameStrategy
        extends StrategyAbstract {
    private final String header_key = "uname";
    private final String strategy_key = "gray-name";

    public ServerUserNameStrategy() {
        setName("S_REGNA_");
        setOrder(5);
    }


    public Server getServer(ILoadBalancer balancer) {
        return getServerByHeader(balancer, "uname", "gray-name");
    }


    public String getName() {
        return this.name;
    }
}
