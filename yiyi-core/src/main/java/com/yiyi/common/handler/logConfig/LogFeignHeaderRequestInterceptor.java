package com.yiyi.common.handler.logConfig;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(prefix = "yiyi", name = {"logheader"}, havingValue = "true")
public class LogFeignHeaderRequestInterceptor
        implements RequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public static final String yiyi_PREFIX = "yiyi-";


    public static final String yiyi_CALL_SERVER = "reqsvr";


    @Value("${app.id:}")
    private String serverName;


    public void apply(RequestTemplate template) {
        try {
            template.header("yiyi-".concat("reqsvr"), new String[]{this.serverName});
        } catch (Exception e) {
            this.logger.error("", e);
        }
    }
}

