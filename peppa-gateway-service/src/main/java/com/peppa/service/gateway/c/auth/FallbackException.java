package com.peppa.service.gateway.c.auth;

public class FallbackException extends RuntimeException {
    public FallbackException(Exception e) {
        super(e);
    }

    public FallbackException(String message) {
        super(message);
    }
}

