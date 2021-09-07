package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.peppa.common.grayconfig.Strategy.factory.AnnotationScan.AnnoStrategy;
import com.peppa.common.grayconfig.ThreadAttributes;
import org.springframework.cloud.netflix.ribbon.eureka.EurekaServerIntrospector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


@AnnoStrategy
public class ServerTagStrategy
        extends StrategyAbstract {
    private final String strategy_key = "gray-tag";

    public ServerTagStrategy() {
        setName("S_TAG_");
        setOrder(3);
    }


    public Server getServer(ILoadBalancer balancer) {
        try {
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
                for (Server server : servers) {
                    Map metadata = serverIntrospector.getMetadata(server);
                    if (metadata != null && metadata.containsKey("gray-tag")) {
                        String metavalue = (String) metadata.get("gray-tag");
                        if (metavalue.equals("no"))
                            continue;
                        try {
                            if (metavalue != null && !metavalue.equals("")) {
                                StringTokenizer stringTokenizer = new StringTokenizer(metavalue, ",", false);
                                while (stringTokenizer.hasMoreTokens()) {
                                    String stategy = stringTokenizer.nextToken().trim();
                                    int ind = stategy.indexOf(":");
                                    if (ind <= 0 || stategy.length() == ind + 1)
                                        continue;
                                    String skey = stategy.substring(0, ind).trim();
                                    String svalue = stategy.substring(ind + 1).trim();
                                    String clientheadervalue = ThreadAttributes.getHeaderValue(skey);
                                    if (clientheadervalue != null && clientheadervalue.equals(svalue)) {
                                        devlist.add(server);
                                    }
                                }

                            }

                        } catch (Exception e) {
                            this.logger.error("gray-tag", e.getMessage());
                        }
                    }
                }
                if (devlist.size() == 0)
                    return null;
                serverRet = getRandom(devlist);

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
            this.logger.error(this.name.concat("选择异常"), e.getMessage());

            return null;
        }
    }

    public String getName() {
        return this.name;
    }
}