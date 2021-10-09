package com.yiyi.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class HeaderAddGatewayFilterFactory extends AbstractGatewayFilterFactory<HeaderAddGatewayFilterFactory.Config> {

    private Logger logger = LoggerFactory.getLogger(HeaderAddGatewayFilterFactory.class);

    public HeaderAddGatewayFilterFactory(){
        super(Config.class);
        logger.info("HeaderAddGatewayFilterFactory init ");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(httpHeaders -> httpHeaders.set(config.getHeaderName(), config.getHeaderValue()))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("headerName","headerValue");
    }

    public static class Config {

        private String headerName;

        private String headerValue;

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getHeaderValue() {
            return headerValue;
        }

        public void setHeaderValue(String headerValue) {
            this.headerValue = headerValue;
        }
    }
}
