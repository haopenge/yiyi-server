package com.yiyi.config;


import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

/**
 * 限流key-规则
 */
@Configuration
public class BeanConfig {

    private Logger logger = LoggerFactory.getLogger(BeanConfig.class);

    @Bean
    public KeyResolver userKeyResolver(){
        return exchange ->
        {
            String user = exchange.getRequest().getQueryParams().getFirst("user");
            logger.info("BeanConfig.userKeyResolver user={}",user);
            assert user != null;
            return Mono.just(user);
        };
    }

    @Bean//拦截请求
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
