package com.peppa.service.gateway.gray;

import brave.Tracer;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.peppa.service.gateway.gray.factory.StrategyFactory;
import com.peppa.service.gateway.utils.ReactiveRequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.StringTokenizer;

@Primary
@Configuration
@Scope("prototype")
@ConditionalOnProperty({"peppa.gray"})
public class PeppaGrayBalanceRule extends RoundRobinRule {
    private static final Logger log = LoggerFactory.getLogger(PeppaGrayBalanceRule.class);


    @Autowired
    private Tracer tracer;


    public Server choose(ILoadBalancer balancer, Object key) {
        ServerHttpRequest request;
        try {
            String threadname = Thread.currentThread().getName();
            if (threadname.contains("zipkin")) {
                return chooseDeault(balancer, key);
            }
        } catch (Exception e) {
            log.error("choose", e);
        }

        if (key instanceof ServerWebExchange) {
            request = ((ServerWebExchange) key).getRequest();
        } else {
            request = getRequest();
        }
        if (request == null) {
            log.warn("非request请求，无法使用灰度配置！");
            return chooseDeault(balancer, key);
        }
        try {
            String whiteliststr = Strategy.getHeaderValue("graywl", request);
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
                    Server server = strategy.getServer(balancer, request);
                    if (server != null) {
                        String strategyname = strategy.getName();
                        log.debug("白名单策略:{}命中服务器:{}", strategyname, server.getHost());
                        return server;
                    }
                }

                log.debug("白名单中的策略没有命中任何服务器{}", whiteliststr);
                return null;
            }

            String blackliststr = Strategy.getHeaderValue("graybl", request);
            log.debug("黑名单:{}", blackliststr);
            for (Strategy strategy : StrategyFactory.getAllStrategy()) {

                String strategyname = strategy.getName();

                if (blackliststr != null &&
                        blackliststr.contains(strategyname)) {
                    continue;
                }

                log.debug("{}策略查找！", strategyname);
                Server server = strategy.getServer(balancer, request);
                if (server != null) {
                    strategyname = strategy.getName();
                    log.debug("策略:{}命中服务器:{}", strategyname, server.getHost());
                    return server;
                }
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
            if (!threadname.contains("zipkin")) {
                log.info("最后缺省随机策略 all server size：{},{}", balancer.getAllServers().size(), server.getHost());
            }
        } catch (Exception e) {
            log.error("chooseDeault", e);
        }
        return server;
    }

    private ServerHttpRequest getRequest() {
        if (this.tracer == null || this.tracer.currentSpan() == null) {
            return null;
        }
        String traceId = this.tracer.currentSpan().context().traceIdString();
        return ReactiveRequestContextHolder.getRequestByTrace(traceId);
    }


    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }
}


