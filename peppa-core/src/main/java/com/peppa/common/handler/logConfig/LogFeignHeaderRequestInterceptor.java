package com.peppa.common.handler.logConfig;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(prefix = "peppa", name = {"logheader"}, havingValue = "true")
public class LogFeignHeaderRequestInterceptor
        implements RequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public static final String HUOHUA_PREFIX = "huohua-";


    public static final String HUOHUA_CALL_SERVER = "reqsvr";


    @Value("${app.id:}")
    private String serverName;


    public void apply(RequestTemplate template) {
        try {
            template.header("huohua-".concat("reqsvr"), new String[]{this.serverName});
        } catch (Exception e) {
            this.logger.error("", e);
        }
    }
}

