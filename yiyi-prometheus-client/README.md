---
theme: smartblue
---
# 1. 本文简介

我们通常使用消息中间件进行消息解耦，来达到削峰填谷的目的，那么解耦后 效果真的达到预期了嘛，如何衡量结果呢？本文即为此而生，通过使用prometheus + grafana 达到记录数据指标，查看实时效果的目的；


先看实现效果：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/563cda0dc9d04a15a0fcd6fb84c98362~tplv-k3u1fbpfcp-zoom-1.image)

这玩意能干嘛？

1） 监听MQ 的消费情况，知晓业务解耦后 削峰填谷 的实际效果；

2） 为合理配置消费线程数 提供数据支持；

3） 消费时长分布为优化业务代码提供数据支持；




> 注：转载请注明出处！！！


# 2. 基础学习

prometheus ： 新一代的云原生监控系统; 与 grafana 数据看板结合，可以很好的实现对 数据库、服务器运行状态 等的监控；

Metrics类型：counter、gauge、summary、histogram



## counter

### 介绍：

只增不减的计数器，常见使用场景：机器运行时间、服务请求总量

### 显示格式：

```
# HELP mq_message_total 当前 TPS.
# TYPE mq_message_total counter
mq_message_total{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 274.0
mq_message_total{application="yiyi-example-prometheus",method="save",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 531.0
```

## gauge

### 介绍：

可增可减的计数器； 常见使用场景：当前运行的线程数、服务运行使用内存大小

### 显示格式：

```bash
# HELP mq_message_working_threads 正在执行工作线程数
# TYPE mq_message_working_threads gauge
mq_message_working_threads{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 0.0
mq_message_working_threads{application="yiyi-example-prometheus",method="save",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 1.0
```

## summary

### 介绍：

记录总量（sum） 、且记录数量(count)，常见使用场景：任务的处理时长分布、CPU的平均使用率；

### 显示格式：

```bash
# HELP mq_message_deal_time 每条消息处理耗时
# TYPE mq_message_deal_time summary
mq_message_deal_time_count{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 274.0
mq_message_deal_time_sum{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 7093.0
mq_message_deal_time_count{application="yiyi-example-prometheus",method="save",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 531.0
mq_message_deal_time_sum{application="yiyi-example-prometheus",method="save",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 13572.0
```

## histogram

### 介绍：

一般用于分析数据的分布情况，记录总量（sum） 、数量(count)、分布情况；



### 显示格式：

```bash
# HELP mq_message_deal_time_histogram 处理时长分组
# TYPE mq_message_deal_time_histogram histogram
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.005",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.01",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.025",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.05",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.075",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.1",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.25",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.5",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="0.75",} 0.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="1.0",} 4.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="2.5",} 11.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="5.0",} 24.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="7.5",} 37.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="10.0",} 55.0
mq_message_deal_time_histogram_bucket{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="+Inf",} 274.0
mq_message_deal_time_histogram_count{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 274.0
mq_message_deal_time_histogram_sum{application="yiyi-example-prometheus",method="update",topic="EAT_FINISH",group="EAT_FINISH_GROUP",} 7093.0
```

# 3. prometheus + grafana服务搭建

prometheus + grafana 启动参见：[点击跳转](https://github.com/haopenge/interview/tree/f45c319a090292674b19bf51aeb528ee93c4dc6c/docker/grafana)


# 4. mq消费监听插件编写

这里以一个小的需求，完成对mq消费性能指标记录；

需求简介：


-   MQ消费者 - 消费消息的TPS数量统计（以秒为单位）；
-   MQ消费者 - 工作线程数统计（以秒为单位）；
-   MQ消费者 - 平均消息处理时长 （以秒为单位）；
-   MQ消费者 - 处理消息的时长 分布情况 （以秒为单位）；




## 4.1 spring-boot 插件编写

### pom.xml

```xml
<!-- =========================== prometheus =========================== -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <version>1.1.4</version>
</dependency>
```




### 核心处理类

#### JobMetrics.java

```java
@Component
public class JobMetrics {

    @Value("${spring.application.name}")
    private String application;

    String [] labelNameArray = {"application", "method", "topic", "group"};

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
     *<pre>
     *     使用此类型，需要提前知晓 le 分组的标签
     *
     * 以下给出 按百分比展示的promQL 语句：
     * ============ 【左边】================
     * sum(rate(mq_message_deal_time_histogram_bucket
     *         {
     * 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="1.0"
     *        }[1m]))
     * /
     * sum(rate(mq_message_deal_time_histogram_count
     *    {
     * 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"
     *    }[1m]))
     *
     *
     * ============ 【中间】================
     *
     *(sum(rate(mq_message_deal_time_histogram_bucket
     *        {
     * 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="2.5"
     *    }[1m]))
     *
     * -sum(rate(mq_message_deal_time_histogram_bucket
     *    {
     * 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="1.0"
     *    }[1m])))
     * /sum(rate(mq_message_deal_time_histogram_count
     *    {
     * 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"
     *    }[1m]))
     *
     *
     * ============ 【后边】================
     *
     *(sum(rate(mq_message_deal_time_histogram_count
     *        {
     * 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"
     *    }[1m]))
     * -sum(rate(mq_message_deal_time_histogram_bucket
     *    {
     * 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="10.0"
     *    }[1m])))
     * /sum(rate(mq_message_deal_time_histogram_count
     *    {
     * 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"
     *    }[1m]))
     *
     *
     *</pre>
     *
     *
     */
     private final Histogram dealTimeHistogram = Histogram.build()
             .name("mq_message_deal_time_histogram")
             .labelNames(labelNameArray)
             .help("处理时长分组")
             .create();


    /**
     *
     * 模拟请求处理，实际使用 可以自行结合AOP 与 公司整合的MQ consumer 进行
     */
    void handleRequest(String method){

        String [] labelArray = {"yiyi-example-prometheus",method,"EAT_FINISH","EAT_FINISH_GROUP"};

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
```

MyJob.java

模拟用户请求

```java
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
```

## 4.2 grafana 显示配置

### 工作线程数统计

#### promQL

```bash
mq_message_working_threads{application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"}
```

#### 消息效果

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/12830627920542ac94f4e76077ca9232~tplv-k3u1fbpfcp-zoom-1.image)




### 消费消息的TPS数量统计

#### promQL

```bash
rate(mq_message_total{topic="EAT_FINISH"}[1m])
```

#### 消息效果

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bc6e036189744fbbb87dbc96b2d13b14~tplv-k3u1fbpfcp-zoom-1.image)

### 平均消息处理时长

#### promQL

```bash
sum(rate(mq_message_deal_time_sum{application="yiyi-example-prometheus", topic="EAT_FINISH",group="EAT_FINISH_GROUP"}[1m]))
/
sum(rate(mq_message_deal_time_count{application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"}[1m]))
```

#### 消息效果




![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cfeb93015c7d4bf0851633eabac4527c~tplv-k3u1fbpfcp-zoom-1.image)

### 处理消息的时长分布情况

#### promQL

```bash
 ============ 【左边】================
 sum(rate(mq_message_deal_time_histogram_bucket
         {
 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="1.0"
        }[1m]))
 /
 sum(rate(mq_message_deal_time_histogram_count
    {
 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"
    }[1m]))


 ============ 【中间】依次配置 多个 ================

(sum(rate(mq_message_deal_time_histogram_bucket
        {
 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="2.5"
    }[1m]))

 -sum(rate(mq_message_deal_time_histogram_bucket
    {
 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="1.0"
    }[1m])))
 /sum(rate(mq_message_deal_time_histogram_count
    {
 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"
    }[1m]))


 ============ 【后边】================

(sum(rate(mq_message_deal_time_histogram_count
        {
 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"
    }[1m]))
 -sum(rate(mq_message_deal_time_histogram_bucket
    {
 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP",le="10.0"
    }[1m])))
 /sum(rate(mq_message_deal_time_histogram_count
    {
 		application="yiyi-example-prometheus",topic="EAT_FINISH",group="EAT_FINISH_GROUP"
    }[1m]))
```

#### 显示效果

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ad2d5b4dc6b74028933fdfaf697194b3~tplv-k3u1fbpfcp-zoom-1.image)




[![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a55599a288b1478f998e93f3a15e7776~tplv-k3u1fbpfcp-zoom-1.image)](https://juejin.cn/user/4424090520391758/posts)

# 参见

-   [prometheus-book](https://yunlzheng.gitbook.io/prometheus-book/)
-   [源码地址-github](https://github.com/haopenge/yiyi-server/tree/7f63c1bc12e8e6376bc85c6ecf8ef7ec1bf3ce02/yiyi-examples/yiyi-example-prometheus)
