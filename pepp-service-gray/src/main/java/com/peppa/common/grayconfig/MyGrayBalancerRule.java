package com.peppa.common.grayconfig;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.peppa.common.grayconfig.Strategy.Strategy;
import com.peppa.common.grayconfig.Strategy.factory.StrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.StringTokenizer;


@Configuration
@ConditionalOnProperty(prefix = "peppa", name = {"gray"}, havingValue = "true")
@Scope("prototype")
@Slf4j
public class MyGrayBalancerRule extends RoundRobinRule {

    public Server choose(ILoadBalancer balancer, Object key) {
        try {
            String threadname = Thread.currentThread().getName();
            if (threadname.indexOf("zipkin") >= 0) {
                return chooseDeault(balancer, key);
            }
        } catch (Exception e) {
            log.error("choose", e);
        }
        try {
            String whiteliststr = ThreadAttributes.getHeaderValue("graywl");
            log.debug("白名单:{}", whiteliststr);

            if (whiteliststr != null) {
                StringTokenizer stringTokenizer = new StringTokenizer(whiteliststr, ",");
                while (stringTokenizer.hasMoreElements()) {
                    String whitestrategy = stringTokenizer.nextToken().trim();

                    if (whitestrategy.equals("DEF")) {
                        return chooseDeault(balancer, key);
                    }
                    Strategy strategy = StrategyFactory.getStrategy(whitestrategy);
                    if (strategy == null) {
                        log.debug("白名单策略:{}不存在", whitestrategy);
                        continue;
                    }
                    Server server = strategy.getServer(balancer);
                    if (server != null) {
                        return server;
                    }
                }
                log.debug("白名单中的策略没有命中任何服务器{}", whiteliststr);
                return null;
            }

            String blackliststr = ThreadAttributes.getHeaderValue("graybl");
            log.debug("黑名单:{}", blackliststr);
            for (Strategy strategy : StrategyFactory.getAllStrategy()) {

                String strategyname = strategy.getName();

                if (blackliststr != null &&
                        blackliststr.indexOf(strategyname) >= 0) {
                    continue;
                }
                log.debug("{}策略生效！", strategyname);
                Server server = strategy.getServer(balancer);
                if (server != null)
                    return server;
            }
        } catch (Exception e) {
            log.error("choose", e);
        }
        return chooseDeault(balancer, key);
    }

    private Server chooseDeault(ILoadBalancer balancer, Object key) {
        Server server = super.choose(balancer, key);
        try {
            String threadname = Thread.currentThread().getName();
            if (threadname.indexOf("zipkin") < 0) {
                log.info("最后缺省随机策略 all server size：{},{}", Integer.valueOf(balancer.getAllServers().size()), server.getHost());
            }
        } catch (Exception e) {
            log.error("chooseDeault", e);
        }
        return server;
    }


    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }
}