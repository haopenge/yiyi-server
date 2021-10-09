package com.yiyi.common.handler.monitor;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class yiyiHealth
        extends AbstractHealthIndicator {
    public static volatile boolean isEurekadown = false;

    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (!isEurekadown) {
            builder.up();
        } else {
            builder.down();
        }
    }
}

