package com.yiyi.service.gateway.config;

import com.yiyi.common.handler.PrometheusServiceCoreConfig;
import com.yiyi.common.handler.apollo.RefreshConfig;
import com.yiyi.common.handler.controller.DiscoveryManagerController;
import com.yiyi.common.handler.controller.check.JarVersionController;
import com.yiyi.common.handler.monitor.PrometheusMonitor;
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

