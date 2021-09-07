package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.peppa.common.grayconfig.Strategy.factory.AnnotationScan.AnnoStrategy;
import com.peppa.common.grayconfig.ThreadAttributes;
import org.springframework.cloud.netflix.ribbon.eureka.EurekaServerIntrospector;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


@AnnoStrategy
public class HeaderServerIplistStrategy
        extends StrategyAbstract {
    private final String header_key = "svips";

    public HeaderServerIplistStrategy() {
        setName("H_SVIP_");
        setOrder(2);
    }


    public Server getServer(ILoadBalancer balancer) {
        String svips = ThreadAttributes.getHeaderValue("svips");
        if (svips == null || svips.equals("")) {
            this.logger.info("HTTP header svips:---null!!!");
            return null;
        }
        this.logger.info("HTTP header svips:---{}", svips);
        StringTokenizer stringTokenizer = new StringTokenizer(svips, ",");
        String serverDiscoverName = null;
        while (stringTokenizer.hasMoreElements()) {
            if (serverDiscoverName == null) {
                serverDiscoverName = getDiscoverName(balancer);
                if (serverDiscoverName == null) {
                    return null;
                }
            }
            String svip = stringTokenizer.nextToken();
            int ind = svip.indexOf(":");
            if (ind <= 0 || svip.length() == ind + 1)
                continue;
            String servername = svip.substring(0, ind).trim();
            String ip = svip.substring(ind + 1).trim();
            if (serverDiscoverName.equals(servername)) {
                Server retserver = getIpSameServer(ip, balancer);
                if (retserver != null) {
                    return retserver;
                }
            }
        }
        return null;
    }


    String getDiscoverName(ILoadBalancer balancer) {
        EurekaServerIntrospector serverIntrospector = new EurekaServerIntrospector();
        List<Server> servers = balancer.getReachableServers();
        Iterator<Server> iterator = servers.iterator();
        if (iterator.hasNext()) {
            Server server = iterator.next();
            return server.getMetaInfo().getAppName();
        }

        return null;
    }

    public String getName() {
        return this.name;
    }
}
