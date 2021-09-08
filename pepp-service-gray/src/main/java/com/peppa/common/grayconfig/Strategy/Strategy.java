package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

public interface Strategy extends Comparable<Strategy> {
    String getName();

    Server getServer(ILoadBalancer paramILoadBalancer);

    int getOrder();
}