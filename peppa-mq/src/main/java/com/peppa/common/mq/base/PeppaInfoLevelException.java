package com.peppa.common.mq.base;

public class PeppaInfoLevelException extends RuntimeException {
    public PeppaInfoLevelException(String message) {
        super(message);
    }

    public PeppaInfoLevelException(String message, Throwable cause) {
        super(message, cause);
    }
}