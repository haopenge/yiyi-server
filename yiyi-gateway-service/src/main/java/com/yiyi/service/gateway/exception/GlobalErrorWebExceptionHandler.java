package com.yiyi.service.gateway.exception;

import com.yiyi.common.response.Response;
import com.yiyi.service.gateway.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Order(-1)
@Configuration
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);


    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatus());
        }

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                Response customResponse = new Response();
                customResponse.failure(ex.getMessage()).code(response.getStatusCode().value());
                return ResponseUtils.getResponeBuffer(response, customResponse);
            } catch (Exception e) {
                log.error("Error writing response", ex);
                return bufferFactory.allocateBuffer(0).write(new byte[0]);
            }
        }));
    }
}
