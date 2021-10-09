package com.yiyi.common.exception;

public class UnauthorizedException
        extends CommonException {
    public UnauthorizedException(int errorCode, String message) {
        super(errorCode, message);
    }
}

