package com.peppa.service.gateway.log;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;


public class CustomServerHttpRequestDecorator extends ServerHttpRequestDecorator {
    private final List<DataBuffer> dataBuffers = new ArrayList<>();

    public CustomServerHttpRequestDecorator(ServerHttpRequest delegate) {
        super(delegate);
        super.getBody().map(dataBuffer -> {
            this.dataBuffers.add(dataBuffer);
            return dataBuffer;
        }).subscribe();
    }


    public Flux<DataBuffer> getBody() {
        return copy();
    }

    private Flux<DataBuffer> copy() {
        return Flux.fromIterable(this.dataBuffers)
                .map(buf -> buf.factory().wrap(buf.asByteBuffer()));
    }
}
