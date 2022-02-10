package com.yiyi.base;

public class YiInfoLevelException extends RuntimeException {
    public YiInfoLevelException(String message) {
        super(message);
    }

    public YiInfoLevelException(String message, Throwable cause) {
        super(message, cause);
    }
}