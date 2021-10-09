package com.yiyi.acc;

import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public interface Idempotent {
    void begin(List<MessageExt> paramList, String paramString) throws Exception;

    void end(List<MessageExt> paramList, String paramString) throws Exception;

    void failed(List<MessageExt> paramList, String paramString) throws Exception;
}