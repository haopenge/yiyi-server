package com.yiyi.acc;

import org.slf4j.MDC;


public interface MqTrace {
    default String productPrintLog() {
        String traceId = MDC.get("traceId");
        return traceId;
    }
}