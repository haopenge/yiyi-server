package com.yiyi.example.config;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class MyJob {


    @Async("main")
    @Scheduled(fixedDelay = 1000)
    public void doSomething() {
        JobMetrics.processRequest();
    }
}
