package com.peppa.common.exception;

public class CommonException
        extends Exception {
    private int code;
    private Object data;

    public CommonException(int errorCode, String message) {
        super(message);
        setCode(errorCode);
    }

    public CommonException(int errorCode, String message, Object data) {
        super(message);
        setCode(errorCode);
        setData(data);
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
