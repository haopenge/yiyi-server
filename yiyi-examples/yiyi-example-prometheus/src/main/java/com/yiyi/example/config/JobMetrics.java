package com.yiyi.example.config;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class JobMetrics {

    @Value("${spring.application.name}")
    private String application;

    String [] labelNameArray = {"application", "instance", "topic", "group"};

    /**
     * 统计mp 消息的 tps，
     * 标签（应用名称，示例ip,topic,group）
     <pre>
         tps统计：   increase(mq_message_total{topic="EAT_FINISH"}[1m])/60
     </pre>

     */
     private final Counter tps = Counter.build()
            .name("mq_message_total")
            .labelNames(labelNameArray)
            .help("当前 TPS.")
            .create();

    /**
     * 统计消息的处理耗时
     <pre>
        单个消息处理平均时长：
             sum(rate(mq_message_deal_time_sum{application="yiyi-example-prometheus", topic="EAT_FINISH",group="EAT_FINISH_GROUP"}[1m]))
             /
             sum(rate(mq_message_deal_time_count{application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"}[1m]))
        单个消息存活时长：
            原理同上
     </pre>
     *
     */
     private final Summary dealTimeSummary = Summary.build()
             .name("mq_message_deal_time")
             .labelNames(labelNameArray)
             .help("每条消息处理耗时")
             .create();


    private final Summary delayTimeSummary = Summary.build()
            .name("mq_message_delay_time")
            .labelNames(labelNameArray)
            .help("每条消息处理延迟")
            .create();


    /**
     * 工作线程数
     */
     private final Gauge workingThreads = Gauge.build()
             .name("mq_message_working_threads")
             .labelNames(labelNameArray)
             .help("正在执行工作线程数")
             .create();

    /**
     * 处理时长分组
     */
     private final Histogram dealTimeHistogram = Histogram.build()
             .name("mq_message_deal_time_histogram")
             .labelNames(labelNameArray)
             .help("处理时长分组")
             .create();



    void handleRequest(String instance){

        String [] labelArray = {"yiyi-example-prometheus",instance,"EAT_FINISH","EAT_FINISH_GROUP"};

        // 处理数组
        workingThreads.labels(labelArray).inc();

        // 消息出生时间
        long bornTime = System.currentTimeMillis() - new Random().nextInt(5) + 1;

        int dealTime = new Random().nextInt(50) + 1;
        try {
            TimeUnit.MILLISECONDS.sleep(dealTime);
        } catch (InterruptedException e) {
            // TODO 异常处理
            e.printStackTrace();
        }
        long delayTime = System.currentTimeMillis() - bornTime;

        tps.labels(labelArray).inc();
        dealTimeSummary.labels(labelArray).observe(dealTime);
        delayTimeSummary.labels(labelArray).observe(delayTime);
        workingThreads.labels(labelArray).dec();
        dealTimeHistogram.labels(labelArray).observe(dealTime);

    }


    @Autowired
    public JobMetrics(PrometheusMeterRegistry meterRegistry) {
        CollectorRegistry prometheusRegistry = meterRegistry.getPrometheusRegistry();
        prometheusRegistry.register(tps);  // TPS
        prometheusRegistry.register(dealTimeSummary); // 任务执行耗时
        prometheusRegistry.register(delayTimeSummary);  // 任务生命周期时长
        prometheusRegistry.register(workingThreads);    // 工作线程数
        prometheusRegistry.register(dealTimeHistogram); // 处理时长分组
    }
}
