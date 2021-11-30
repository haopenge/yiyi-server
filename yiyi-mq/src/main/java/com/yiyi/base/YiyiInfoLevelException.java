package com.yiyi.base;

public class YiyiInfoLevelException extends RuntimeException {
    public YiyiInfoLevelException(String message) {
        super(message);
    }

    public YiyiInfoLevelException(String message, Throwable cause) {
        super(message, cause);
    }
}