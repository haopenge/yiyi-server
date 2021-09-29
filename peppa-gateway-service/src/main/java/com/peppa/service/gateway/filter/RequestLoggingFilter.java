//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.peppa.service.gateway.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.peppa.service.gateway.utils.JsonUtils;
import com.peppa.service.gateway.utils.ReactiveRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final List<String> LOGKEYS = Lists.newArrayList("uri", "ip", "method", "param", "code");
    private static final String ELAPSED_TIME_KEY = "ELAPSED_TIME";

    public RequestLoggingFilter() {
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put("ELAPSED_TIME", startTime);
        Route route = (Route)exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        URI routeUri = (URI)exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        return chain.filter(exchange).doOnEach(logOnEach((r) -> {
            log.info("gateway access route_id: {}, route_uri: {}, url: {}, referer: {}", route.getId(), routeUri, request.getURI(), ReactiveRequestUtils.getReferer(request));
        })).subscriberContext(Context.of(this.generateContextMap(exchange))).then(Mono.fromRunnable(() -> {
            Long startTimeInContext = (Long)exchange.getAttribute("ELAPSED_TIME");
            if (startTimeInContext != null) {
                Long executeTime = System.currentTimeMillis() - startTimeInContext;
                log.info("gateway response route_id: {}, route_uri: {}, url: {}, status: {}, cost(ms): {}", route.getId(), routeUri, request.getURI(), exchange.getResponse().getStatusCode(), executeTime);
            }

        }));
    }

    private Map<String, String> generateContextMap(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        Map<String, String> contextMap = Maps.newHashMap();
        contextMap.put("uri", request.getPath().value());
        contextMap.put("ip", ReactiveRequestUtils.getRealIp(request));
        contextMap.put("method", request.getMethodValue());
        contextMap.put("param", JsonUtils.obj2String(request.getQueryParams()));
        contextMap.put("code", exchange.getResponse().getStatusCode().value() + "");
        return contextMap;
    }

    private static <T> Consumer<Signal<T>> logOnEach(Consumer<T> logStatement) {
        return (signal) -> {
            try {
                LOGKEYS.stream().forEach((r) -> {
                    MDC.putCloseable(r, (String)signal.getContext().getOrDefault(r, ""));
                });
                logStatement.accept(signal.get());
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        };
    }

    public int getOrder() {
        return -2147483648;
    }
}
