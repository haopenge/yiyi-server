package com.peppa.common.response;

import java.io.Serializable;


public class Response<T>
        implements Serializable {
    private static final long serialVersionUID = 157482155539246939L;
    private static final String OK = "";
    private static final String ERROR = "error";
    private boolean success;
    private String message;
    private int code;
    private T data;

    public Response success() {
        this.success = true;
        this.message = "";
        return this;
    }

    public Response failure() {
        this.success = false;
        this.message = "error";
        return this;
    }

    public Response failure(String message) {
        this.success = false;
        this.message = message;
        return this;
    }

    public Response data(T data) {
        this.data = data;
        return this;
    }

    public Response code(int code) {
        this.code = code;
        return this;
    }

    public Response message(String message) {
        this.message = message;
        return this;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData() {
        return this.data;
    }
}
