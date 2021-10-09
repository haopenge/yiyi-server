package com.yiyi.common.response;

public interface GlobalResultCode {
    int getCode();

    String getMessage();

    public enum Request
            implements GlobalResultCode {
        InvalidArguments(-330001, "无效参数"), MissingRequiredArguments(-330002, "缺少参数"),
        ResourceNotFound(-330003, "资源不存在"), Unauthorized(-330004, "未登录"),
        MethodNotSupported(-330005, "请求类型不支持"),
        InvalidSecret(-330006, "签名错误");

        private int errorCode;
        private String message;

        Request(int errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public int getCode() {
            return this.errorCode;
        }

        public String getMessage() {
            return this.message;
        }
    }

    public enum Internal implements GlobalResultCode {
        ServiceError(-440001, "服务内部错误"),
        RpcError(-440002, "服务内部错误");

        private int errorCode;
        private String message;

        Internal(int errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public int getCode() {
            return this.errorCode;
        }

        public String getMessage() {
            return this.message;
        }
    }

    public enum Server
            implements GlobalResultCode {
        ServiceError(500, "服务内部错误"),
        RpcError(501, "服务内部错误");
        private int errorCode;
        private String message;

        Server(int errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public int getCode() {
            return this.errorCode;
        }

        public String getMessage() {
            return this.message;
        }
    }
}
