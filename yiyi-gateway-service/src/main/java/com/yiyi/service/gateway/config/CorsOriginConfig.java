package com.yiyi.service.gateway.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;


@Configuration
public class CorsOriginConfig {
    @Value("${spring.cloud.gateway.allowed_headers:}")
    private String allowedHeaders;
    @Value("${spring.cloud.gateway.allowed_methods:*}")
    private String allowedMethods;
    @Value("${spring.cloud.gateway.allowed_origin:*}")
    private String allowedOrigin;
    @Value("${spring.cloud.gateway.allowed_expose:*}")
    private String allowedExpose;
    private String MAX_AGE = "18000L";

    @Value("${spring.cloud.gateway.cors:false}")
    private boolean corsOpen;

    @Bean
    public WebFilter corsFilter() {
        return (ctx, chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            if (this.corsOpen && CorsUtils.isCorsRequest(request)) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                headers.add("Access-Control-Allow-Origin", this.allowedOrigin);
                headers.add("Access-Control-Allow-Methods", this.allowedMethods);
                headers.add("Access-Control-Max-Age", this.MAX_AGE);
                headers.add("Access-Control-Allow-Headers", this.allowedHeaders);
                headers.add("Access-Control-Expose-Headers", this.allowedExpose);
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            return chain.filter(ctx);
        };
    }
}
