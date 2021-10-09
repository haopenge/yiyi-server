package com.yiyi.base;

public class yiyiMqException extends RuntimeException {
    public yiyiMqException(String message) {
        super(message);
    }

    public yiyiMqException(String message, Throwable cause) {
        super(message, cause);
    }
}
