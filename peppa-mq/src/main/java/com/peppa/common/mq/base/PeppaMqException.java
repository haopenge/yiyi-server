package com.peppa.common.mq.base;

public class PeppaMqException extends RuntimeException {
    public PeppaMqException(String message) {
        super(message);
    }

    public PeppaMqException(String message, Throwable cause) {
        super(message, cause);
    }
}
