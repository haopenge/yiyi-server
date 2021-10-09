package com.yiyi.base;

public class yiyiInfoLevelException extends RuntimeException {
    public yiyiInfoLevelException(String message) {
        super(message);
    }

    public yiyiInfoLevelException(String message, Throwable cause) {
        super(message, cause);
    }
}