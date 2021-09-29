package com.peppa.service.gateway.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;


public class ReactiveRequestContextHolder {
    public static final Class<ServerHttpRequest> REQUEST_CONTEXT_KEY = ServerHttpRequest.class;

    private static final ConcurrentHashMap<String, ServerHttpRequest> TRACE_REQUEST = new ConcurrentHashMap<>();

    public static void put(String traceId, ServerHttpRequest request) {
        if (request != null) {
            TRACE_REQUEST.put(traceId, request);
        }
    }

    public static void removeByTrace(String traceId) {
        TRACE_REQUEST.remove(traceId);
    }

    public static ServerHttpRequest getRequestByTrace(String traceId) {
        ServerHttpRequest request = TRACE_REQUEST.get(traceId);
        return request;
    }


    public static Mono<ServerHttpRequest> getRequestFromContext() {
        return Mono.subscriberContext().map(ctx -> (ServerHttpRequest) ctx.get(REQUEST_CONTEXT_KEY));
    }
}
