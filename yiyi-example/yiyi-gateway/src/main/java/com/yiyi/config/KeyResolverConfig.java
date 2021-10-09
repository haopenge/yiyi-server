package com.yiyi.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 限流key-规则
 */
@Configuration
public class KeyResolverConfig {

    private Logger logger = LoggerFactory.getLogger(KeyResolverConfig.class);

    @Bean
    public KeyResolver userKeyResolver(){
        return exchange ->
        {
            String user = exchange.getRequest().getQueryParams().getFirst("user");
            logger.info("KeyResolverConfig.userKeyResolver user={}",user);
            assert user != null;
            return Mono.just(user);
        };
    }
}
