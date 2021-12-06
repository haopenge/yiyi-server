package com.yiyi.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 模拟2太机器 处理 消息
 */
@Component
@EnableScheduling
public class MyJob {

    @Autowired
    private JobMetrics jobMetrics;


    @Async("main")
    @Scheduled(fixedDelay = 500)
    public void tpsRequestHandle1() {
        jobMetrics.handleRequest("save");
    }


    @Async("main")
    @Scheduled(fixedDelay = 1000)
    public void tpsRequestHandle2() {
        jobMetrics.handleRequest("update");
    }



}
