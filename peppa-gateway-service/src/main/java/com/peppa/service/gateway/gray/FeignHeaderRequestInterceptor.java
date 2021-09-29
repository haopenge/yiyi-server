package com.peppa.service.gateway.gray;


import brave.Tracer;
import com.peppa.service.gateway.utils.ReactiveRequestContextHolder;
import com.peppa.service.gateway.utils.ReactiveRequestUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;
import java.util.Map;


@Configuration
@ConditionalOnProperty({"peppa.gray"})
public class FeignHeaderRequestInterceptor
        implements RequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public static final String HTTP_X_FORWARDED_FOR = "x-forwarded-for";


    public static final String HUOHUA_PREFIX = "huohua-";


    public static final String HUOHUA_CALL_IP = "call-ip";


    @Autowired
    private Tracer tracer;


    public void apply(RequestTemplate template) {
        try {
            ServerHttpRequest request = getRequest();
            if (request == null) {
                this.logger.warn("could not get request from context...");
                return;
            }
            HttpHeaders headers = request.getHeaders();
            boolean find_xforwardid = false;
            if (headers != null) {
                for (Map.Entry<String, List<String>> entry : (Iterable<Map.Entry<String, List<String>>>) headers.entrySet()) {

                    if (((String) entry.getKey()).equalsIgnoreCase("x-forwarded-for")) {
                        template.header(entry.getKey(), new String[]{CollectionUtils.isNotEmpty(entry.getValue()) ? ((List<String>) entry.getValue()).get(0) : ""});
                        find_xforwardid = true;
                    }

                    if (((String) entry.getKey()).startsWith("huohua-")) {
                        String values = CollectionUtils.isNotEmpty(entry.getValue()) ? ((List<String>) entry.getValue()).get(0) : "";
                        template.header(entry.getKey(), new String[]{values});
                    }
                }
            }


            if (!find_xforwardid) {
                String remoteAddr = ReactiveRequestUtils.getRealIp(request);
                template.header("x-forwarded-for", new String[]{remoteAddr});
            }
            this.logger.info("feign interceptor header:{}", template);
        } catch (Exception e) {
            this.logger.error("", e);
        }
    }

    private ServerHttpRequest getRequest() {
        if (this.tracer == null || this.tracer.currentSpan() == null) {
            return null;
        }
        String traceId = this.tracer.currentSpan().context().traceIdString();
        ServerHttpRequest requestInTrace = ReactiveRequestContextHolder.getRequestByTrace(traceId);
        return requestInTrace;
    }
}

