package com.peppa.common.mq.base;

import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqClient {
  String groupName() default "";
  
  int maxThread() default 5;
  
  int minThread() default 2;
  
  int batchSize() default 32;
  
  String topic() default "";
  
  String tag() default "*";
  
  ConsumeMode modeType() default ConsumeMode.PUSH;
  
  boolean orderly() default false;
  
  int moveDate() default 2020120113;
  
  MessageModel model() default MessageModel.CLUSTERING;
}