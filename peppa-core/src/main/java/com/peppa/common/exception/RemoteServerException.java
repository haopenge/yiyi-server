package com.peppa.common.exception;

import lombok.Data;

@Data
public class RemoteServerException extends RuntimeException {

    private int code;
    private Object data;

    public RemoteServerException(int errorCode, String message) {
        super(message);
        this.setCode(errorCode);
    }

    public RemoteServerException(int errorCode, String message, Object data) {
        super(message);
        this.setCode(errorCode);
        this.setData(data);
    }

}
