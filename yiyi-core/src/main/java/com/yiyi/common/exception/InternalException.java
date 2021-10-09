package com.yiyi.common.exception;

public class InternalException
        extends CommonException {
    public InternalException(int errorCode, String message) {
        super(errorCode, message);
    }

    public InternalException(int errorCode, String message, Object data) {
        super(errorCode, message, data);
    }
}
