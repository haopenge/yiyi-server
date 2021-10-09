package com.yiyi.common.base;

import com.yiyi.common.exception.CommonException;
import com.yiyi.common.exception.InternalException;
import com.yiyi.common.exception.UnauthorizedException;
import com.yiyi.common.response.GlobalResultCode;
import com.yiyi.common.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;


public abstract class BaseController {
    protected Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected Response success() {
        return (new Response()).success().code(HttpStatus.OK.value());
    }

    protected Response success(Object data) {
        return (new Response()).success().data(data);
    }

    protected Response failure() {
        return (new Response()).failure().code(GlobalResultCode.Server.ServiceError.getCode());
    }

    protected Response failure(String message) {
        return (new Response()).code(HttpStatus.INTERNAL_SERVER_ERROR.value()).failure(message);
    }

    protected Response failure(int errorCode, String message) {
        return (new Response()).code(errorCode).failure(message);
    }

    protected void throwError(GlobalResultCode errorEnum) throws CommonException {
        if (errorEnum.getClass() == GlobalResultCode.Internal.class) {
            throw new InternalException(errorEnum.getCode(), errorEnum.getMessage());
        }
        throw new CommonException(errorEnum.getCode(), errorEnum.getMessage());
    }


    protected void throwError(GlobalResultCode errorEnum, Object data) throws CommonException {
        if (errorEnum.getClass() == GlobalResultCode.Internal.class) {
            throw new InternalException(errorEnum.getCode(), errorEnum.getMessage(), data);
        }
        throw new CommonException(errorEnum.getCode(), errorEnum.getMessage(), data);
    }


    protected void throwError(GlobalResultCode errorEnum, String message) throws CommonException {
        if (errorEnum.getClass() == GlobalResultCode.Internal.class) {
            throw new InternalException(errorEnum.getCode(), message);
        }
        throw new CommonException(errorEnum.getCode(), message);
    }


    protected void throwError(GlobalResultCode errorEnum, String message, Object data) throws CommonException {
        if (errorEnum.getClass() == GlobalResultCode.Internal.class) {
            throw new InternalException(errorEnum.getCode(), message, data);
        }
        throw new CommonException(errorEnum.getCode(), message, data);
    }


    protected void throwError(int errorCode, String message) throws CommonException {
        throw new CommonException(errorCode, message);
    }


    protected void throwUnauthorizedException() throws UnauthorizedException {
        throw new UnauthorizedException(GlobalResultCode.Request.Unauthorized.getCode(), GlobalResultCode.Request.Unauthorized.getMessage());
    }


    protected void throwError(int errorCode, String message, Object data) throws CommonException {
        throw new CommonException(errorCode, message, data);
    }
}

