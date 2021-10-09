package com.yiyi.service.gateway.auth;

import com.yiyi.common.response.Response;


public class FallbackResponse<T>
        extends Response<T> {
    public static boolean isFallback(Response response) {
        return response instanceof FallbackResponse;
    }

    public static FallbackResponse getInstance() {
        return new FallbackResponse();
    }
}