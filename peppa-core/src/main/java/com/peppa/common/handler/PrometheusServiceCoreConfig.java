package com.peppa.common.handler;

import com.peppa.common.handler.monitor.GitInfoMonitor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class PrometheusServiceCoreConfig {
    @Bean
    MeterRegistryCustomizer<MeterRegistry> applicationNameConfig(@Value("${spring.application.name:unknown}") String applicationName) {
        return registry -> registry.config().commonTags(new String[]{"application", applicationName});
    }


    @Bean
    public GitInfoMonitor myEndPoint() {
        return new GitInfoMonitor();
    }
}

