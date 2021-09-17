package com.peppa.common.handler.pool;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import feign.Feign;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({Feign.class})
@AutoConfigureBefore({FeignAutoConfiguration.class})
public class HttpPoolConfig {
    @Bean
    public OkHttpClient okHttpClient() {
        Config appConfig = ConfigService.getAppConfig();
        Integer maxIdleConnections = appConfig.getIntProperty("peppa.feign.pool.max-idle-connections", Integer.valueOf(3));
        return OkHttpClientBuilder.build(maxIdleConnections.intValue());
    }
}
