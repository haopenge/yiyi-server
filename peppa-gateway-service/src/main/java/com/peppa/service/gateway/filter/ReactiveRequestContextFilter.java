package com.peppa.service.gateway.filter;

import com.peppa.service.gateway.utils.ReactiveRequestContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@ConditionalOnWebApplication(
        type = Type.REACTIVE
)
public class ReactiveRequestContextFilter implements WebFilter, Ordered {
    public ReactiveRequestContextFilter() {
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        return chain.filter(exchange)
                .subscriberContext((ctx) -> ctx.put(ReactiveRequestContextHolder.REQUEST_CONTEXT_KEY, request))
                .doFinally((s) -> Mono.subscriberContext().subscribe((ctx) -> ctx.delete(request)));
    }

    public int getOrder() {
        return -2147483642;
    }
}
