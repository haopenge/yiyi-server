package com.peppa.service.gateway.b.auth;

import com.peppa.common.response.Response;

public class FallbackResponse<T> extends Response<T> {
    public boolean isFallBack() {
        return this.isFallBack;
    }

    private boolean isFallBack = true;
}

