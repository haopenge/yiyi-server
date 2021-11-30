package com.yiyi.base;

public class YiyiMqException extends RuntimeException {
    public YiyiMqException(String message) {
        super(message);
    }

    public YiyiMqException(String message, Throwable cause) {
        super(message, cause);
    }
}
