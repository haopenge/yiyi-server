package com.yiyi.example.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务生命周期监控
 */
@Component
public class ServerLifeNotify  implements SmartLifecycle {

    private Logger logger = LoggerFactory.getLogger(ServerLifeNotify.class);
    private  AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        logger.info("yiyi-example server start finish");
        running.set(true);
    }

    @Override
    public void stop() {
        logger.info("yiyi-example server top finish");
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }
}
