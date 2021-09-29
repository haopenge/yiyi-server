package com.peppa.service.gateway.config;

import com.peppa.common.handler.PrometheusServiceCoreConfig;
import com.peppa.common.handler.apollo.RefreshConfig;
import com.peppa.common.handler.controller.DiscoveryManagerController;
import com.peppa.common.handler.controller.check.JarVersionController;
import com.peppa.common.handler.monitor.PrometheusMonitor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({RefreshConfig.class, PrometheusMonitor.class, PrometheusServiceCoreConfig.class, JarVersionController.class, DiscoveryManagerController.class})
public class CommonConfig {
    static {
        System.setProperty("reactor.netty.pool.leasingStrategy", "lifo");
        System.setProperty("reactor.netty.http.server.accessLogEnabled", "true");
    }
}

