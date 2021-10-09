package com.yiyi.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class CorsGatewayFilterFactory extends AbstractGatewayFilterFactory<CorsGatewayFilterFactory.Config> {

    private Logger logger = LoggerFactory.getLogger(CorsGatewayFilterFactory.class);

    @Value("${yiyi.gateway.allowed_headers:}")
    private String allowedHeaders;

    @Value("${yiyi.gateway.allowed_methods:}")
    private String allowedMethods;

    @Value("${yiyi.gateway.allowed_origins:}")
    private String allowedOrigin;

    @Value("${yiyi.gateway.allowed_expose_headers:}")
    private String alllowedExposeHeaders;

    @Value("${yiyi.gateway.max_age:18000L}")
    private String maxAge;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if(Objects.equals(config.corsOpenStatus,"1") || !CorsUtils.isCorsRequest(request) ){ // 未开启，不是跨域请求-返回
                return chain.filter(exchange);
            }

            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();
            headers.add("Access-Control-Allow-Origin", this.allowedOrigin);
            headers.add("Access-Control-Allow-Methods", this.allowedMethods);
            headers.add("Access-Control-Allow-Headers", this.allowedHeaders);
            headers.add("Access-Control-Max-Age", maxAge);
            headers.add("Access-Control-Expose-Headers", this.alllowedExposeHeaders);

            if(Objects.equals(request.getMethod(), HttpMethod.OPTIONS)){
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
            return chain.filter(exchange);
        };
    }

    public CorsGatewayFilterFactory(){
        super(Config.class);
        logger.info("CorsGatewayFilterFactory init");
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("corsOpenStatus");
    }

    public static class Config{
        private String corsOpenStatus;

        public String getCorsOpenStatus() {
            return corsOpenStatus;
        }

        public void setCorsOpenStatus(String corsOpenStatus) {
            this.corsOpenStatus = corsOpenStatus;
        }
    }
}
