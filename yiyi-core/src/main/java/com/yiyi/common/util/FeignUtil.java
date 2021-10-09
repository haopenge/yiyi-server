package com.yiyi.common.util;

import com.yiyi.common.exception.RemoteServerException;
import com.yiyi.common.response.Response;

public class FeignUtil {

    private FeignUtil() {
    }

    /**
     * 获取微服务返回对象(根据具体业务getData有可能为null需要自己处理)
     *
     * @param result 返回数据
     * @param <T>    返回值类型
     * @return 返回值
     */
    public static <T> T getResponseData(Response<T> result) {
        if (!result.isSuccess()) {
            throw new RemoteServerException(result.getCode(), result.getMessage(), result.getData());
        }
        return result.getData();
    }

}


