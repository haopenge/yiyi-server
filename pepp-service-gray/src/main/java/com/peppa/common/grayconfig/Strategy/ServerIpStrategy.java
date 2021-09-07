package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.peppa.common.grayconfig.Strategy.factory.AnnotationScan.AnnoStrategy;
import com.peppa.common.grayconfig.ThreadAttributes;


@AnnoStrategy
public class ServerIpStrategy
        extends StrategyAbstract {
    private final String strategy_key = "gray-ip";

    public ServerIpStrategy() {
        setName("S_REGIP_");
        setOrder(4);
    }


    public Server getServer(ILoadBalancer balancer) {
        String ip = (String) ThreadAttributes.getThreadAttribute("x-forwarded-for");
        if (ip == null || ip.equals("")) {
            this.logger.info("HTTP_X_FORWARDED_FOR threadlocal ip:---null!!!");
            return null;
        }
        return getServerByRegex(balancer, ip, "gray-ip");
    }

    public String getName() {
        return this.name;
    }
}
