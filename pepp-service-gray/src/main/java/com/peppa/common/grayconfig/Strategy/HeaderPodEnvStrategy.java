package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.peppa.common.grayconfig.Strategy.factory.AnnotationScan.AnnoStrategy;
import com.peppa.common.grayconfig.ThreadAttributes;
import org.springframework.cloud.netflix.ribbon.eureka.EurekaServerIntrospector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@AnnoStrategy
public class HeaderPodEnvStrategy
        extends StrategyAbstract {
    private final String strategy_key = "podenv";

    public HeaderPodEnvStrategy() {
        setName("H_PODENV_");
        setOrder(7);
    }


    public Server getServer(ILoadBalancer balancer) {
        try {
            String clientheadervalue = ThreadAttributes.getHeaderValue("podenv");
            if (clientheadervalue == null || clientheadervalue.trim().equals("")) {
                return null;
            }
            EurekaServerIntrospector serverIntrospector = new EurekaServerIntrospector();
            Server serverRet = null;
            int count = 0;


            while (serverRet == null && count++ < 5) {
                List<Server> servers = balancer.getAllServers();
                List<Server> serversReach = balancer.getReachableServers();
                if (serversReach.size() == 0 || servers.size() == 0) {
                    return null;
                }

                List<Server> devlist = new ArrayList<>();
                List<Server> podenvlist = new ArrayList<>();
                for (Server server : servers) {
                    Map metadata = serverIntrospector.getMetadata(server);
                    String verkey = "ver";
                    if (metadata.containsKey(verkey)) {
                        String value = (String) metadata.get(verkey);
                        if (value.equals("no")) {
                            continue;
                        }

                        if (clientheadervalue.equals(value)) {
                            podenvlist.add(server);
                        }
                    }
                }


                if (devlist.size() == 0 && podenvlist.size() == 0) {
                    return null;
                }
                serverRet = (podenvlist.size() > 0) ? getRandom(podenvlist) : getRandom(devlist);

                if (serverRet == null) {
                    Thread.yield();
                } else if (serverRet.isAlive() && serverRet.isReadyToServe()) {
                    return serverRet;
                }

                serverRet = null;
            }

            if (count >= 5) {
                this.logger.warn("No available alive servers after 5 tries from load balancer: " + balancer);
            }

            return serverRet;
        } catch (Exception e) {
            this.logger.error("HeaderPodEnvStrategy 选择异常", e.getMessage());

            return null;
        }
    }

    public String getName() {
        return this.name;
    }
}
