//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.peppa.service.gateway.filter;

import brave.propagation.TraceContext;
import com.peppa.service.gateway.utils.ReactiveRequestContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@ConditionalOnProperty({"peppa.gray"})
@Configuration
@ConditionalOnWebApplication(
        type = Type.REACTIVE
)
public class ReactiveRequestTraceContextFilter implements WebFilter, Ordered {
    public ReactiveRequestTraceContextFilter() {
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        return chain.filter(exchange).subscriberContext((ctx) -> {
            TraceContext traceContext = ctx.get(TraceContext.class);
            ReactiveRequestContextHolder.put(traceContext.traceIdString(), request);
            return ctx;
        }).doFinally((s) -> {
            Mono.subscriberContext().subscribe((ctx) -> {
                TraceContext traceContext = ctx.get(TraceContext.class);
                ReactiveRequestContextHolder.removeByTrace(traceContext.traceIdString());

            });
        });
    }

    public int getOrder() {
        return -2147483642;
    }
}
