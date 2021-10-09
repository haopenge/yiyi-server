package com.yiyi.service.gateway.filter;


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


@Component
public class CorsGatewayFilterFactory extends AbstractGatewayFilterFactory<CorsGatewayFilterFactory.Config> {
    private static final Logger log = LoggerFactory.getLogger(CorsGatewayFilterFactory.class);

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

    public CorsGatewayFilterFactory() {
        super(Config.class);
    }


    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (!this.corsOpen && CorsUtils.isCorsRequest(request)) {
                ServerHttpResponse response = exchange.getResponse();
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
            return chain.filter(exchange);
        };
    }

    public static class Config {
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Config)) return false;
            Config other = (Config) o;
            return other.canEqual(this);
        }

        protected boolean canEqual(Object other) {
            return other instanceof Config;
        }

        public int hashCode() {
            int result = 1;
            return 1;
        }

        public String toString() {
            return "CorsGatewayFilterFactory.Config()";
        }

    }
}

