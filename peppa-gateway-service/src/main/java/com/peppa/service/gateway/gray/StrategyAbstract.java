package com.peppa.service.gateway.gray;


import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.ribbon.eureka.EurekaServerIntrospector;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;


public class StrategyAbstract implements Strategy {
    final Logger logger = LoggerFactory.getLogger(getClass());
    public String name = "";
    private int order = 0;


    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    public int compareTo(Strategy strategy) {
        return this.order - strategy.getOrder();
    }


    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }


    public Server getServer(ILoadBalancer balancer, ServerHttpRequest request) {
        return null;
    }


    public Server getServerByHeader(ILoadBalancer balancer, String header_key, String strategy_key, ServerHttpRequest request) {
        String headervalue = Strategy.getHeaderValue(header_key, request);
        if (headervalue == null || headervalue.trim().equals("")) {
            return null;
        }
        return getServerByRegex(balancer, headervalue.trim(), strategy_key);
    }


    public Server getServerByRegex(ILoadBalancer balancer, String feature_value, String strategy_key) {
        try {
            EurekaServerIntrospector serverIntrospector = new EurekaServerIntrospector();
            Server serverRet = null;
            int count = 0;


            while (count++ < 5) {
                List<Server> servers = balancer.getAllServers();
                List<Server> serversReach = balancer.getReachableServers();
                if (serversReach.size() == 0 || servers.size() == 0) {
                    return null;
                }

                List<Server> devlist = new ArrayList<>();
                for (Server server : servers) {
                    Map metadata = serverIntrospector.getMetadata(server);
                    if (metadata.containsKey(strategy_key)) {
                        String metavalue = (String) metadata.get(strategy_key);
                        if (metadata == null) {
                            continue;
                        }
                        metavalue = metavalue.trim();
                        if (metavalue.equals("no")) {
                            continue;
                        }
                        try {
                            if (!metavalue.equals("") && Pattern.matches(metavalue, feature_value)) {
                                devlist.add(server);
                            }
                        } catch (Exception e) {
                            this.logger.error(strategy_key, e.getMessage());
                        }
                    }
                }
                if (devlist.size() == 0) {
                    return null;
                }
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


    Server getRandom(List<Server> devlist) {
        int devsize = devlist.size();
        if (devsize == 1) {
            try {
                this.logger.info(this.name.concat("策略命中一个Server").concat(((Server) devlist.get(0)).getHost()));
            } catch (Exception e) {
                this.logger.error("getRandom", e);
            }
            return devlist.get(0);
        }
        if (devsize > 1) {
            int number = ThreadLocalRandom.current().nextInt(devsize);
            try {
                this.logger.info(String.format("%s策略命中%d个Server,choose%d", this.name, devsize, number).concat(devlist.get(number).getHost()));
            } catch (Exception e) {
                this.logger.error("getRandom", e);
            }
            return devlist.get(number);
        }
        return null;
    }


    Server getIpSameServer(String ip, ILoadBalancer balancer) {
        try {
            Server serverRet = null;
            int count = 0;


            while (count++ < 5) {
                List<Server> servers = balancer.getAllServers();
                List<Server> serversReach = balancer.getReachableServers();
                if (serversReach.size() == 0 || servers.size() == 0) {
                    return null;
                }
                List<Server> devlist = new ArrayList<>();


                for (Server server : servers) {
                    if (server != null && server.getHost().equals(ip)) {
                        devlist.add(server);
                    }
                }

                if (devlist.size() == 0) {
                    return null;
                }
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
            this.logger.error("ip相等判断异常", e.getMessage());

            return null;
        }
    }
}

