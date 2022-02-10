package com.yiyi.consumer;

import brave.Span;
import com.alibaba.fastjson.JSONObject;
import com.yiyi.acc.Idempotent;
import com.yiyi.base.ConsumeMode;
import com.yiyi.base.MqClient;
import com.yiyi.base.YiInfoLevelException;
import com.yiyi.base.YiyiMqException;
import com.yiyi.span.MqSpan;
import org.apache.rocketmq.client.consumer.*;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class YiyiMqConsumer implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(YiyiMqConsumer.class);

    private String namesServerAddress;
    private String consumerGroup;
    private String instanceName;
    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

    private ConcurrentHashMap<String, Thread> consumersMap;

    private ConcurrentHashMap<String, DefaultMQPushConsumer> defaultMQPushConsumerConcurrentHashMap;

    private ConcurrentHashMap<String, MQPullConsumerScheduleService> mqPullConsumerScheduleServiceConcurrentHashMap;

    private final Object object = new Object();

    private Map<String, MqConsumerCallback> cCallbackMap;

    private final Map<String, Integer> extGroup = new ConcurrentHashMap<>();

    private volatile boolean isIdempotent = false;

    private static Idempotent idempotent;

    private String idempotentFactoryClass = "com.yiyi.common.mq.acc.impl.IdempotentFactory";

    public static void setIdempotent(Idempotent idempotent) {
        YiyiMqConsumer.idempotent = idempotent;
    }

    private boolean isGray = false;

    private String serverPodEnv;

    private boolean isLazy = true;

    public void initConsumerNoLazy() {
        this.isLazy = false;
        initConsumer();
    }


    public void initConsumer() {
        try {
            Class.forName(this.idempotentFactoryClass);
            log.warn("mq 幂等生效");
        } catch (ClassNotFoundException e) {
            log.warn("无幂等jar，幂等不生效");
        }
        try {
            Class.forName("com.yiyi.common.handler.mqstarter.MqConsumerStarter");
            log.warn("MQ消费者初始化，检查到com.yiyi.common.handler.mqstarter.MqConsumerStarter！");
        } catch (ClassNotFoundException e) {
            this.isLazy = false;
            log.warn("MQ消费者初始化错误，请升级yiyi-service-core版本，目前MQ listener懒加载不生效！");
        }
        if (idempotent instanceof Idempotent) {
            this.isIdempotent = true;
        }

        String isgraystr = System.getProperty("mq_gray");
        if (isgraystr == null) {
            isgraystr = System.getenv("mq_gray");
        }
        if ("enable_gray".equals(isgraystr)) {
            this.isGray = true;

            this.serverPodEnv = System.getProperty("podenv");
            if (this.serverPodEnv == null || this.serverPodEnv.length() == 0) {
                this.serverPodEnv = System.getenv("podenv");
            }
            if (this.serverPodEnv == null) {
                this.serverPodEnv = "";
            }
            log.warn("MQ Consumer灰度配置生效！podenv:{}", this.serverPodEnv);
        } else {
            log.warn("MQ消费者灰度无效！");
        }
        if (this.consumersMap == null) {
            synchronized (this.object) {
                if (this.consumersMap == null) {
                    this.consumersMap = new ConcurrentHashMap<>();
                    this.defaultMQPushConsumerConcurrentHashMap = new ConcurrentHashMap<>();
                    this.mqPullConsumerScheduleServiceConcurrentHashMap = new ConcurrentHashMap<>();
                    this.cCallbackMap.forEach((className, mqConsumerCallback) -> executorTask(mqConsumerCallback));
                }
            }
        }
    }


    private String realConsumerName(String groupName, String topic, MqConsumerCallback mqConsumerCallback, Method method) {
        String realName;
        if (groupName != null && !groupName.isEmpty()) {
            realName = groupName;
        } else {
            getGroupIndex(topic);
            realName = this.consumerGroup + "-" + topic + getAbs(mqConsumerCallback, method, topic);
        }
        return realName;
    }


    public void createDefaultMQPushConsumer(MessageModel messageModel, final MqConsumerCallback mqConsumerCallback, final Method method, final String topic, String tag, boolean orderly, final boolean isList, String groupName, int minThread, int maxThread) throws MQClientException {
        log.warn("进行createDefaultMQPushConsumer");

        final String publicGroupName = realConsumerName(groupName, topic, mqConsumerCallback, method);
        final String realConsumerName = dealGroupNameGray(publicGroupName);
        final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(realConsumerName);
        consumer.setNamesrvAddr(this.namesServerAddress);
        if (this.instanceName == null) {
            consumer.setInstanceName(this.consumerGroup);
        } else {
            consumer.setInstanceName(this.instanceName);
        }

        consumer.setMessageModel(messageModel);
        consumer.subscribe(topic, tag);
        consumer.setConsumeFromWhere(this.consumeFromWhere);
        consumer.setConsumeThreadMin(minThread);
        consumer.setConsumeThreadMax(maxThread);
        long start = System.currentTimeMillis();
        if (orderly) {
            consumer.registerMessageListener(new MessageListenerOrderly() {
                public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
                    MqSpan span = new MqSpan();
                    Span s = null;
                    try {
                        if (YiyiMqConsumer.this.isIdempotent) {
                            try {
                                YiyiMqConsumer.idempotent.begin(list, realConsumerName);
                            } catch (YiyiMqException pe) {
                                YiyiMqConsumer.log.warn("消息重复, e", (Throwable) pe);
                                return ConsumeOrderlyStatus.SUCCESS;
                            } catch (Exception e) {
                                YiyiMqConsumer.log.error("幂等处理未知错误,请联系技术处理，同时幂等失效，e", e);
                            }
                        }
                        if (isList) {
                            if (YiyiMqConsumer.this.isGray) {

                                List<MessageExt> listchecked = YiyiMqConsumer.this.getCheckDiscardList(list, consumer, topic, publicGroupName);
                                if (listchecked.size() > 0) {
                                    s = span.start(listchecked, method.getName(), topic);
                                    method.invoke(mqConsumerCallback, listchecked);
                                    span.end(s);
                                }
                            } else {
                                s = span.start(list, method.getName(), topic);
                                method.invoke(mqConsumerCallback, list);
                                span.end(s);
                            }
                        } else {

                            for (MessageExt messageExt : list) {
                                if (!YiyiMqConsumer.this.checkDiscard(messageExt, consumer, topic, publicGroupName)) {
                                    s = span.start(messageExt, method.getName(), topic);
                                    method.invoke(mqConsumerCallback, messageExt);
                                    span.end(s);
                                }
                            }
                        }
                        return ConsumeOrderlyStatus.SUCCESS;
                    } catch (YiyiMqException pe) {
                        YiyiMqConsumer.log.info("重复消费，返回消费成功, e", (Throwable) pe);
                        span.error(s, (Throwable) pe);
                        return ConsumeOrderlyStatus.SUCCESS;
                    } catch (YiInfoLevelException pile) {
                        YiyiMqConsumer.log.info("pile push处理顺序消息异常  e", (Throwable) pile);

                        try {
                            list.forEach(me -> YiyiMqConsumer.log.info("pile push order error:{}", JSONObject.toJSONString(me)));

                        } catch (Exception e1) {
                            YiyiMqConsumer.log.info("pile 打印日志出错，e", (Throwable) pile);
                        }
                        try {
                            if (YiyiMqConsumer.this.isIdempotent) {
                                YiyiMqConsumer.idempotent.failed(list, realConsumerName);
                            }
                        } catch (Exception e1) {
                            YiyiMqConsumer.log.info("pile 幂等异常：e", (Throwable) pile);
                        }
                        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    } catch (Exception e) {
                        YiyiMqConsumer.log.error("push处理顺序消息异常  e", e);
                        span.error(s, e);
                        try {
                            list.forEach(me -> YiyiMqConsumer.log.info("push order error:{}", JSONObject.toJSONString(me)));

                        } catch (Exception e1) {
                            YiyiMqConsumer.log.error("打印日志出错，e", e);
                        }
                        try {
                            if (YiyiMqConsumer.this.isIdempotent) {
                                YiyiMqConsumer.idempotent.failed(list, realConsumerName);
                            }
                        } catch (Exception e1) {
                            YiyiMqConsumer.log.error("幂等异常：e", e);
                        }
                        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    }
                }
            });
        } else {
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    MqSpan span = new MqSpan();
                    Span s = null;
                    try {
                        if (YiyiMqConsumer.this.isIdempotent) {
                            try {
                                YiyiMqConsumer.idempotent.begin(list, realConsumerName);
                            } catch (YiyiMqException pe) {
                                YiyiMqConsumer.log.warn("消息重复, e", (Throwable) pe);
                                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                            } catch (Exception e) {
                                YiyiMqConsumer.log.error("幂等处理未知错误,请联系技术处理，同时幂等失效，e", e);
                            }
                        }
                        if (isList) {
                            if (YiyiMqConsumer.this.isGray) {

                                List<MessageExt> listchecked = YiyiMqConsumer.this.getCheckDiscardList(list, consumer, topic, publicGroupName);
                                if (listchecked.size() > 0) {
                                    s = span.start(listchecked, method.getName(), topic);
                                    method.invoke(mqConsumerCallback, new Object[]{listchecked});
                                    span.end(s);
                                }
                            } else {
                                s = span.start(list, method.getName(), topic);
                                method.invoke(mqConsumerCallback, new Object[]{list});
                                span.end(s);
                            }
                        } else {
                            for (MessageExt messageExt : list) {
                                if (!YiyiMqConsumer.this.checkDiscard(messageExt, consumer, topic, publicGroupName)) {
                                    s = span.start(messageExt, method.getName(), topic);
                                    method.invoke(mqConsumerCallback, new Object[]{messageExt});
                                    span.end(s);
                                }
                            }
                        }
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    } catch (YiyiMqException pe) {
                        YiyiMqConsumer.log.info("重复消费，返回消费成功, e", (Throwable) pe);
                        span.error(s, (Throwable) pe);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    } catch (YiInfoLevelException pile) {
                        YiyiMqConsumer.log.info("pile push处理消息异常,延时重新消费 e", (Throwable) pile);


                        try {
                            if (YiyiMqConsumer.this.isIdempotent) {
                                YiyiMqConsumer.idempotent.failed(list, realConsumerName);
                            }
                        } catch (Exception e1) {
                            YiyiMqConsumer.log.info("pile 幂等异常：e", (Throwable) pile);
                        }
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } catch (Exception e) {
                        YiyiMqConsumer.log.error("push处理消息异常,延时重新消费 e ", e);
                        span.error(s, e);


                        try {
                            if (YiyiMqConsumer.this.isIdempotent) {
                                YiyiMqConsumer.idempotent.failed(list, realConsumerName);
                            }
                        } catch (Exception e1) {
                            YiyiMqConsumer.log.error("幂等异常：e", e);
                        }
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
            });
        }
        if (!this.isLazy) {
            consumer.start();
            log.info("topic:{} start!", topic);
        }
        this.defaultMQPushConsumerConcurrentHashMap.put(realConsumerName, consumer);
    }


    public void createMQPullConsumerScheduleService(MessageModel messageModel, final MqConsumerCallback mqConsumerCallback, final Method method, final String topic, final String tag, final boolean isList, String groupName, final int batchSize) throws MQClientException {
        String realName = realConsumerName(groupName, topic, mqConsumerCallback, method);
        MQPullConsumerScheduleService mqPullConsumerScheduleService = new MQPullConsumerScheduleService(realName);
        DefaultMQPullConsumer defaultMQPullConsumer = mqPullConsumerScheduleService.getDefaultMQPullConsumer();
        defaultMQPullConsumer.setNamesrvAddr(this.namesServerAddress);
        defaultMQPullConsumer.setMessageModel(messageModel);
        if (this.instanceName == null) {
            defaultMQPullConsumer.setInstanceName(this.consumerGroup);
        } else {
            defaultMQPullConsumer.setInstanceName(this.instanceName);
        }

        mqPullConsumerScheduleService.registerPullTaskCallback(topic, new PullTaskCallback() {
            public void doPullTask(MessageQueue mq, PullTaskContext pullTaskContext) {
                MQPullConsumer consumer = pullTaskContext.getPullConsumer();
                MqSpan span = new MqSpan();
                Span s = null;
                try {
                    List<MessageExt> msgFoundList;
                    long offset = consumer.fetchConsumeOffset(mq, false);
                    if (offset < 0L) {
                        offset = 0L;
                    }
                    PullResult pullResult = consumer.pull(mq, tag, offset, batchSize);
                    switch (pullResult.getPullStatus()) {
                        case FOUND:
                            msgFoundList = pullResult.getMsgFoundList();


                            if (isList) {
                                try {
                                    s = span.start(msgFoundList, method.getName(), topic);
                                    method.invoke(mqConsumerCallback, new Object[]{msgFoundList});
                                    span.end(s);
                                } catch (YiyiMqException pe) {
                                    YiyiMqConsumer.log.info("重复消费，返回消费成功, e", (Throwable) pe);
                                    consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
                                    pullTaskContext.setPullNextDelayTimeMillis(100);
                                    span.error(s, (Throwable) pe);
                                } catch (YiInfoLevelException pile) {
                                    YiyiMqConsumer.log.info("pile pull list 业务处理失败,e", (Throwable) pile);

                                    boolean b = YiyiMqConsumer.this.pullRetry(method, mqConsumerCallback, msgFoundList);
                                    if (!b) {
                                        msgFoundList.forEach(me -> YiyiMqConsumer.log.info("pile pull list error:{}", JSONObject.toJSONString(me)));

                                    }

                                } catch (Exception e) {
                                    YiyiMqConsumer.log.error("pull list 业务处理失败,e", e);

                                    boolean b = YiyiMqConsumer.this.pullRetry(method, mqConsumerCallback, msgFoundList);
                                    if (!b) {
                                        msgFoundList.forEach(me -> YiyiMqConsumer.log.info("pull list error:{}", JSONObject.toJSONString(me)));
                                    }


                                    span.error(s, e);
                                }
                                break;
                            }
                            for (MessageExt messageExt : msgFoundList) {
                                try {
                                    s = span.start(messageExt, method.getName(), topic);
                                    method.invoke(mqConsumerCallback, new Object[]{messageExt});
                                    span.end(s);
                                } catch (YiyiMqException pe) {
                                    YiyiMqConsumer.log.info("重复消费，返回消费成功，e", (Throwable) pe);
                                    span.error(s, (Throwable) pe);
                                    consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
                                    pullTaskContext.setPullNextDelayTimeMillis(100);
                                } catch (YiInfoLevelException pile) {
                                    YiyiMqConsumer.log.info("pile pull messageExt 业务处理失败,e", (Throwable) pile);

                                    boolean b = YiyiMqConsumer.this.pullRetry(method, mqConsumerCallback, messageExt);
                                    if (!b) {
                                        YiyiMqConsumer.log.info("pile pull object error:{}", JSONObject.toJSONString(messageExt));
                                    }
                                } catch (Exception e) {
                                    YiyiMqConsumer.log.error("pull messageExt 业务处理失败,e", e);
                                    span.error(s, e);
                                    boolean b = YiyiMqConsumer.this.pullRetry(method, mqConsumerCallback, messageExt);
                                    if (!b) {
                                        YiyiMqConsumer.log.info("pull object error:{}", JSONObject.toJSONString(messageExt));
                                    }
                                }
                            }
                            break;
                    }


                    consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
                    pullTaskContext.setPullNextDelayTimeMillis(100);
                } catch (Exception e) {
                    YiyiMqConsumer.log.error("topic:{} consume message error, e", topic, e);
                    e.printStackTrace();
                }
            }
        });
        mqPullConsumerScheduleService.start();
        this.mqPullConsumerScheduleServiceConcurrentHashMap.put(realName, mqPullConsumerScheduleService);
    }


    private int getClientThreadSize() {
        int size = 0;
        Set<Map.Entry<String, MqConsumerCallback>> entries = this.cCallbackMap.entrySet();
        for (Map.Entry<String, MqConsumerCallback> map : entries) {
            MqConsumerCallback mqConsumerCallback = map.getValue();
            Class<? extends MqConsumerCallback> classz = (Class) mqConsumerCallback.getClass();
            Method[] methods = classz.getMethods();
            size += methods.length;
        }
        return size;
    }

    private boolean checkParameter(Parameter[] parameters) {
        boolean isList;
        if (parameters != null && parameters.length > 0) {
            Parameter parameter = parameters[0];
            String name = parameter.getType().getName();
            if ("java.util.List".equals(name)) {
                isList = true;
            } else if ("org.apache.rocketmq.common.message.MessageExt".equals(name)) {
                isList = false;
            } else {
                throw new YiyiMqException("consumer 参数类型错误");
            }
        } else {
            throw new YiyiMqException("consumer 方法没指定参数");
        }
        return isList;
    }

    private void executorTask(final MqConsumerCallback mqConsumerCallback) {
        Method[] methods;
        Class<? extends MqConsumerCallback> classz = (Class) mqConsumerCallback.getClass();
        String name = classz.getName();

        boolean isSpringProxy = (name.indexOf("SpringCGLIB$$") >= 0);
        if (isSpringProxy) {
            methods = ReflectionUtils.getAllDeclaredMethods(AopUtils.getTargetClass(mqConsumerCallback));
        } else {
            methods = classz.getMethods();
        }
        Thread workThread = null;
        for (Method method : methods) {
            MqClient annotation = null;
            if (isSpringProxy) {
                annotation = (MqClient) AnnotationUtils.findAnnotation(method, MqClient.class);
            } else {
                annotation = method.<MqClient>getAnnotation(MqClient.class);
            }
            if (annotation != null) {
                Parameter[] parameters = method.getParameters();
                final boolean isList = checkParameter(parameters);
                final String topic = annotation.topic();
                final String tag = annotation.tag();
                final boolean orderly = annotation.orderly();
                final String groupName = annotation.groupName();
                final int minThread = annotation.minThread();
                final int maxThread = annotation.maxThread();
                final int batchSize = annotation.batchSize();
                ConsumeMode consumeMode = annotation.modeType();
                final MessageModel messageModel = annotation.model();
                String threadName = realConsumerName(groupName, topic, mqConsumerCallback, method);
                if (consumeMode.equals(ConsumeMode.PUSH)) {
                    workThread = new Thread(new Runnable() {
                        public void run() {
                            try {
                                YiyiMqConsumer.log.info("pushConsumer start, topic:{},tag:{}", topic, tag);
                                YiyiMqConsumer.this.createDefaultMQPushConsumer(messageModel, mqConsumerCallback, method, topic, tag, orderly, isList, groupName, minThread, maxThread);
                                YiyiMqConsumer.log.info("pushConsumer start, topic:{},tag:{},status:{}", topic, tag, "success");
                            } catch (Exception e) {
                                YiyiMqConsumer.log.error("pushConsumer start failed, topic:{},tag:{}，e:" + e, topic, tag);
                            }
                        }
                    }, threadName);
                    workThread.start();
                    this.consumersMap.put(threadName, workThread);
                }
                if (consumeMode.equals(ConsumeMode.PULL)) {
                    workThread = new Thread(new Runnable() {
                        public void run() {
                            try {
                                YiyiMqConsumer.log.info("pullConsumer start, topic:{},tag:{}", topic, tag);
                                YiyiMqConsumer.this.createMQPullConsumerScheduleService(messageModel, mqConsumerCallback, method, topic, tag, isList, groupName, batchSize);
                                YiyiMqConsumer.log.info("pullConsumer start failed, topic:{},tag:{},status:{}", topic, tag, "success");
                            } catch (Exception e) {
                                YiyiMqConsumer.log.error("pullConsumer start failed, topic:{},tag:{}", topic, tag);
                            }
                        }
                    }, threadName);
                    workThread.start();
                    this.consumersMap.put(threadName, workThread);
                }
            }
        }
    }

    public void setcCallbackMap(Map<String, MqConsumerCallback> cCallbackMap) {
        this.cCallbackMap = cCallbackMap;
    }

    public void setNamesServerAddress(String namesServerAddress) {
        this.namesServerAddress = namesServerAddress;
    }

    public void start() {
        if (this.isLazy) {
            log.info("mq消费者 延迟启动成功!");
            Collection<DefaultMQPushConsumer> pushValues = this.defaultMQPushConsumerConcurrentHashMap.values();
            pushValues.forEach(value -> {
                try {
                    value.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }
    }


    public void close() throws IOException {
        Collection<DefaultMQPushConsumer> pushValues = this.defaultMQPushConsumerConcurrentHashMap.values();
        pushValues.forEach(value -> value.shutdown());


        Collection<MQPullConsumerScheduleService> pullValues = this.mqPullConsumerScheduleServiceConcurrentHashMap.values();
        pullValues.forEach(value -> value.shutdown());


        Collection<Thread> threadsValues = this.consumersMap.values();
        threadsValues.forEach(threadValue -> threadValue.stop());
    }


    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }


    private synchronized String getGroupIndex(String topic) {
        Integer integer = this.extGroup.get(topic);
        if (integer == null) {
            this.extGroup.put(topic, Integer.valueOf(0));
            return "";
        }
        integer = Integer.valueOf(integer.intValue() + 1);
        this.extGroup.put(topic, integer);
        return String.valueOf(integer);
    }

    private synchronized String getAbs(MqConsumerCallback mqConsumerCallback, Method method, String topic) {
        Integer i = this.extGroup.get(topic);
        if (i.intValue() == 0) {
            return "";
        }

        int absInt = getAbsInt(mqConsumerCallback, method);
        return String.valueOf(absInt);
    }

    private synchronized int getAbsInt(MqConsumerCallback mqConsumerCallback, Method method) {
        String clazzName = mqConsumerCallback.getClass().getName();
        String methodName = method.getName();
        String CNMN = clazzName + methodName;
        return Math.abs(CNMN.hashCode());
    }

    private boolean pullRetry(Method method, MqConsumerCallback mqConsumerCallback, Object object) {
        boolean isSuccess = false;
        int time = 1;
        while (time <= 3) {
            try {
                method.invoke(mqConsumerCallback, new Object[]{object});
                isSuccess = true;
                break;
            } catch (Exception ex) {
                log.error("pull 重试业务处理失败，当前重试次数：{}", Integer.valueOf(time));

                time++;
            }
        }
        return isSuccess;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public ConsumeFromWhere getConsumeFromWhere() {
        return this.consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public String getIdempotentFactoryClass() {
        return this.idempotentFactoryClass;
    }

    public void setIdempotentFactoryClass(String idempotentFactoryClass) {
        this.idempotentFactoryClass = idempotentFactoryClass;
    }

    private List<MessageExt> getCheckDiscardList(List<MessageExt> list, DefaultMQPushConsumer consumer, String topic, String groupName) {
        List<MessageExt> newlist = new ArrayList<>();
        for (MessageExt messageExt : list) {
            if (!checkDiscard(messageExt, consumer, topic, groupName)) {
                newlist.add(messageExt);
            }
        }
        return newlist;
    }


    private boolean checkDiscard(MessageExt messageExt, DefaultMQPushConsumer consumer, String topic, String groupName) {
        if (!this.isGray) {
            return false;
        }


        String rulepodenv = getTopicGroupRulePodenv(topic, groupName);

        String mespodenv = messageExt.getProperty("podenv");

        if (rulepodenv != null && rulepodenv.length() > 0) {

            if (checkGrayRules(topic, groupName, this.serverPodEnv)) {
                log.info("我是独立环境{},apollo规则指定了目标环境{},所以我消费掉了(topic:{},group:{})", new Object[]{this.serverPodEnv, rulepodenv, topic, groupName});
                addHeaderToThreadLocal(this.serverPodEnv);
                return false;
            }

            if (checkOtherGrayRulesConsumed(consumer, topic, groupName)) {
                log.info("我是独立环境{},apollo规则指定了目标环境{},目标环境存在，我抛弃掉(topic:{},group:{})", new Object[]{this.serverPodEnv, rulepodenv, topic, groupName});
                return true;
            }

            if (this.serverPodEnv == null || this.serverPodEnv.trim().length() == 0 || "qa".equals(this.serverPodEnv) || "dev".equals(this.serverPodEnv) || "sim".equals(this.serverPodEnv)) {
                log.info("我是独立环境{},apollo规则指定了目标环境{},而没有目标环境存在,我是公共服务，所以我消费掉了(topic:{},group:{})", new Object[]{this.serverPodEnv, rulepodenv, topic, groupName});
                addHeaderToThreadLocal(this.serverPodEnv);
                return false;
            }
            log.info("我是独立环境{},apollo规则指定了目标环境{},而没有目标环境存在，但我不是公共服务，所以我抛弃掉(topic:{},group:{})", new Object[]{this.serverPodEnv, rulepodenv, topic, groupName});
            return true;
        }


        if (this.serverPodEnv == null || this.serverPodEnv.trim().length() == 0 || "qa".equals(this.serverPodEnv) || "dev".equals(this.serverPodEnv) || "sim".equals(this.serverPodEnv)) {
            if (mespodenv == null || mespodenv.trim().length() == 0 || mespodenv.equals("qa") || mespodenv.equals("dev") || mespodenv.equals("sim")) {

                if (this.serverPodEnv == null || this.serverPodEnv.trim().length() == 0) {
                    log.info("我是公共环境,消息中携带标记{},我消费掉(topic:{},group:{})", new Object[]{mespodenv, topic, groupName});
                } else {
                    log.info("我是公共环境{},消息中携带标记{},我消费掉(topic:{},group:{})", new Object[]{this.serverPodEnv, mespodenv, topic, groupName});
                }

                addHeaderToThreadLocal(this.serverPodEnv);
                return false;
            }
            try {
                MQClientInstance mqClientInstance = consumer.getDefaultMQPushConsumerImpl().getmQClientFactory();
                String groupNameFind = grayGroupName(groupName, mespodenv);
                log.info("topic:{},原始消费组:{}，独立环境消费组:{}", new Object[]{topic, groupName, groupNameFind});

                List<String> ret = mqClientInstance.findConsumerIdList(topic, groupNameFind);
                if (ret == null || ret.size() == 0) {

                    log.info("我是独立环境{},消息中携带标记{},没有这个目标环境存在，我是公共环境，所以消费了(topic:{},group:{})", new Object[]{this.serverPodEnv, mespodenv, topic, groupName});
                    addHeaderToThreadLocal(mespodenv);
                    return false;
                }
            } catch (Exception ex) {
                log.error("寻找mq的consumer出错", ex);
                return false;
            }

            log.info("我是独立环境{},消息中携带标记{},该目标环境存在，所以我抛弃掉(topic:{},group:{})", new Object[]{this.serverPodEnv, mespodenv, topic, groupName});
            return true;
        }


        if (this.serverPodEnv.equals(mespodenv)) {
            log.info("我是独立环境{},消息中携带标记{},所以我消费掉(topic:{},group:{})", new Object[]{this.serverPodEnv, mespodenv, topic, groupName});
            addHeaderToThreadLocal(this.serverPodEnv);
            return false;
        }
        log.info("我是独立环境{},消息中携带标记{},所以我抛弃掉(topic:{},group:{})", new Object[]{this.serverPodEnv, mespodenv, topic, groupName});
        return true;
    }


    private boolean checkGrayRules(String topicName, String groupName, String objPodenv) {
        String rulepodenv = getTopicGroupRulePodenv(topicName, groupName);
        if (rulepodenv == null || rulepodenv.length() == 0) {
            return false;
        }
        if (rulepodenv.equals(objPodenv)) {
            return true;
        }
        return false;
    }

    private boolean checkOtherGrayRulesConsumed(DefaultMQPushConsumer consumer, String topicName, String groupName) {
        try {
            MQClientInstance mqClientInstance = consumer.getDefaultMQPushConsumerImpl().getmQClientFactory();
            String rulepodenv = getTopicGroupRulePodenv(topicName, groupName);

            if (rulepodenv == null || rulepodenv.length() == 0) {
                return false;
            }

            String groupNameFind = grayGroupName(groupName, rulepodenv);
            log.info("mq消费者检查checkOtherGrayRulesConsumed rulepodenv:{},topic:{},原始消费组:{}，独立环境消费组:{}", new Object[]{rulepodenv, topicName, groupName, groupNameFind});

            List<String> ret = mqClientInstance.findConsumerIdList(topicName, groupNameFind);
            if (ret == null || ret.size() == 0) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.error("寻找mq的consumer出错", ex);

            return false;
        }
    }

    private String getTopicGroupRulePodenv(String topicName, String groupName) {
        try {
            Class<?> threadlocalClazz = Class.forName("com.yiyi.common.grayconfig.apolloconfig.MqGrayApolloConfig");
            Method method = threadlocalClazz.getMethod("getTopicGroupPodEnv", new Class[]{String.class});
            String podenv = (String) method.invoke((Object) null, new Object[]{topicName + "." + groupName});

            if (podenv != null) {
                return podenv;
            }
            method = threadlocalClazz.getMethod("getTopicPodEnv", new Class[]{String.class});
            podenv = (String) method.invoke((Object) null, new Object[]{topicName});
            if (podenv != null) {
                return podenv;
            }
        } catch (ClassNotFoundException e) {
            log.error("mq消费者架构灰度组件没有提供！");
        } catch (Exception e) {
            log.error("mqconsumer灰度解析失败！", e);
        }
        return null;
    }

    private String dealGroupNameGray(String groupName) {
        if (!this.isGray) {
            return groupName;
        }
        return grayGroupName(groupName, this.serverPodEnv);
    }

    private String grayGroupName(String groupName, String podenv) {
        if (podenv != null && podenv.trim().length() > 0 && !podenv.equals("qa") && !podenv.equals("dev") && !podenv.equals("sim")) {
            String podenvModi = podenv.replace("/", "-");
            return groupName + "_PEV_" + podenvModi;
        }
        return groupName;
    }

    private void addHeaderToThreadLocal(String podenv) {
        if (podenv == null || podenv.trim().length() == 0) {
            return;
        }

        try {
            Class<?> threadlocalClazz = Class.forName("com.yiyi.common.grayconfig.ThreadAttributes");
            Method method = threadlocalClazz.getMethod("setThreadAttribute", String.class, Object.class);
            method.invoke((Object) null, "yiyi-podenv", podenv);
        } catch (Exception e) {
            log.warn("mq消费者{}没有找到，没有引用架构组件！微服务环境必须引用！", "com.yiyi.common.grayconfig.ThreadAttributes");
        }
    }
}