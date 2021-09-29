package com.peppa.service.gateway.utils;

import com.peppa.common.response.Response;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class ResponseUtils {
    public static DataBuffer getResponeBuffer(ServerHttpResponse httpResponse, Response response) {
        byte[] bits = JsonUtils.obj2String(response).getBytes(StandardCharsets.UTF_8);


        DataBuffer buffer = httpResponse.bufferFactory().allocateBuffer(bits.length).write(bits);
        return buffer;
    }

    public static Mono<Void> generateBuffer(ServerHttpResponse httpResponse, Response response) {
        httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = getResponeBuffer(httpResponse, response);
        return httpResponse.writeWith(Mono.just(buffer));
    }

    public static Mono<Void> generateInvalidClientBuffer(ServerHttpResponse httpResponse) {
        Response responseFailed = (new Response()).code(401).failure("The client_id in request is not invalid...");
        httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = getResponeBuffer(httpResponse, responseFailed);
        return httpResponse.writeWith(Mono.just(buffer));
    }
}
