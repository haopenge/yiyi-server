package com.peppa.service.gateway.c.auth;

import com.peppa.common.response.Response;


public class FallbackResponse<T>
        extends Response<T> {
    public static boolean isFallback(Response response) {
        return response instanceof FallbackResponse;
    }

    public static FallbackResponse getInstance() {
        return new FallbackResponse();
    }
}