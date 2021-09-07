package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.peppa.common.grayconfig.Strategy.factory.AnnotationScan.AnnoStrategy;
import com.peppa.common.grayconfig.ThreadAttributes;

import java.util.StringTokenizer;


@AnnoStrategy
public class HeaderIplistStrategy
        extends StrategyAbstract {
    private final String header_key = "ips";

    public HeaderIplistStrategy() {
        setName("H_IPS_");
        setOrder(1);
    }


    public Server getServer(ILoadBalancer balancer) {
        String ips = ThreadAttributes.getHeaderValue("ips");
        if (ips == null || ips.equals("")) {
            this.logger.info("HTTP header ips:---null!!!");
            return null;
        }
        this.logger.info("HTTP header ips:---{}", ips);
        StringTokenizer stringTokenizer = new StringTokenizer(ips, ",");
        while (stringTokenizer.hasMoreTokens()) {
            String ip = stringTokenizer.nextToken().trim();
            Server retserver = getIpSameServer(ip, balancer);
            if (retserver != null) {
                return retserver;
            }
        }
        return null;
    }

    public String getName() {
        return this.name;
    }
}