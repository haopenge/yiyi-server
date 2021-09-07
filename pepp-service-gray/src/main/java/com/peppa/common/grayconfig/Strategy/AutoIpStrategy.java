package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.peppa.common.grayconfig.Strategy.factory.AnnotationScan.AnnoStrategy;
import com.peppa.common.grayconfig.ThreadAttributes;
import com.peppa.common.util.IpUtils;


@AnnoStrategy
public class AutoIpStrategy
        extends StrategyAbstract {
    public AutoIpStrategy() {
        setName("A_IP_");
        setOrder(6);
    }


    public Server getServer(ILoadBalancer balancer) {
        String ip = (String) ThreadAttributes.getThreadAttribute("x-forwarded-for");
        if (ip == null) {
            ip = IpUtils.getMyIp();
        }
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
