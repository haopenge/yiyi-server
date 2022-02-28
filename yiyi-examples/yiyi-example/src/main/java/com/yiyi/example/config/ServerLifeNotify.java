package com.yiyi.example.config;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2,
                new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-%d")
                        .setDaemon(true)
                        .build());
        scheduler.schedule(new EatTask(),5, TimeUnit.SECONDS);

        try {
            Thread.sleep(1000 * 60 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class EatTask extends Thread{
        @Override
        public void run() {
             System.out.println("-------->  start eat");
        }
    }

}
