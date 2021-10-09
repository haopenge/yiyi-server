package com.yiyi.common.exception;

public class ServiceException extends RuntimeException {
    private int code;

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public ServiceException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode;
    }

    public ServiceException(int errorCode, String message) {
        super(message);
        this.code = errorCode;
    }

    public ServiceException(int errorCode, Throwable cause) {
        super(cause);
        this.code = errorCode;
    }
}

