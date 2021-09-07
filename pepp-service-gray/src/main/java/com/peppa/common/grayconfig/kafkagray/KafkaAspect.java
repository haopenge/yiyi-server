package com.peppa.common.grayconfig.kafkagray;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.PeppaGrayPartitionAssignor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;


@Aspect
@Service
@ConditionalOnClass({PeppaGrayPartitionAssignor.class})
@ConditionalOnProperty(prefix = "peppa", name = {"gray"}, havingValue = "true")
public class KafkaAspect {
    public static final String GRAY_STRATEGY_CLASS = "org.apache.kafka.clients.consumer.PeppaGrayPartitionAssignor";
    public static final String ENVTAGB = "#ENV-";
    public static final String ENVTAGE = "-ENV#";
    private static final Logger logger = LoggerFactory.getLogger(KafkaAspect.class);
    public static String podenvTag;
    @Value("${podenv:}")
    String podenv;
    @Value("${app.id}")
    String appname;
    private static boolean ISENABLE = false;

    public KafkaAspect() {
        logger.info("KafkaAspect=========================");
    }


    public static String getPodenvFromConsumerId(String counsmerId) {
        int posb = counsmerId.indexOf("#ENV-");
        if (posb < 0) {
            return null;
        }
        int pose = counsmerId.indexOf("-ENV#");
        if (pose < 0) {
            return null;
        }
        String env = counsmerId.substring(posb + "#ENV-".length(), pose);
        return env;
    }

    @PostConstruct
    public void init() {
        podenvTag = this.appname + "#ENV-" + this.podenv + "-ENV#";
    }


    @Pointcut("execution(* org.springframework.kafka.core.DefaultKafkaConsumerFactory.createConsumer(..)) ")
    public void createConsumerCut() {
    }


    @Around("createConsumerCut()")
    public Object aroundConsumerAspect(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Class.forName("org.apache.kafka.clients.consumer.PeppaGrayPartitionAssignor");
        } catch (Exception e) {
            logger.error("启用了灰度模式，但是kafka 客户端版本非火花定制版本！请联系架构组解决！");
            ISENABLE = false;
            return pjp.proceed();
        }
        ISENABLE = true;


        Object[] args = pjp.getArgs();
        args[1] = podenvTag + args[1];


        if (args.length > 3) {
            if (args[3] != null) {
                ((Map<String, String>) args[3])
                        .put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.PeppaGrayPartitionAssignor");
            } else {
                Properties properties = new Properties();
                properties.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.PeppaGrayPartitionAssignor");
                args[3] = properties;
            }
        }


        return pjp.proceed(args);
    }


    @Pointcut("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public void consumerCut() {
    }

    @Around("consumerCut()")
    public Object aroundAspect(ProceedingJoinPoint pjp) throws Throwable {
        if (!ISENABLE) {
            return pjp.proceed();
        }
        MethodSignature msig = (MethodSignature) pjp.getSignature();

        Method targetMethod = pjp.getTarget().getClass().getDeclaredMethod(msig.getName(), msig.getMethod().getParameterTypes());


        KafkaListener annotation = targetMethod.<KafkaListener>getAnnotation(KafkaListener.class);
        String groupid = annotation.id();
        if (groupid == null) {
            groupid = annotation.groupId();
        }
        TopicPartition[] topicPartitions = annotation.topicPartitions();

        if (topicPartitions != null && topicPartitions.length > 0) {


            ConsumerRecord record = (ConsumerRecord) pjp.getArgs()[0];

            if (isThisPodenvConsume(record, groupid)) {
                return pjp.proceed();
            }
            return null;
        }

        return pjp.proceed();
    }


    private boolean isThisPodenvConsume(ConsumerRecord record, String groupid) {
        String topic = record.topic();
        int partition = record.partition();
        String apollokey = topic + "." + groupid + "." + partition;
        String needpodenv = KafkaPartitionGrayApolloConfig.getTopicPartitionPodEnv(apollokey);
        if (needpodenv == null) {
            logger.info("apollo 参数未指定：{}:{},正常消费:{}", new Object[]{apollokey, this.podenv, record});
            return true;
        }
        if (needpodenv.equals(this.podenv)) {
            logger.info("apollo 参数指定了：{}:{},匹配本次消费:{}", new Object[]{apollokey, this.podenv, record});
            return true;
        }
        logger.info("apollo 参数指定了：{}:{},但我是{},忽略本次消费:{}", new Object[]{apollokey, needpodenv, this.podenv, record});
        return false;
    }
}
