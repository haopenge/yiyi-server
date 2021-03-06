package com.yiyi.producter;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;


public class YiyiMqProducter implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(YiyiMqProducter.class);


    private String namesServerAddress;


    private String producerGroup;

    private String instanceName;


    private final int persistConsumerOffsetInterval = 5000;


    private final int sendMsgTimeout = 10000;


    private final int compressMsgBodyOverHowmuch = 4096;


    private final boolean retryAnotherBrokerWhenNotStoreOK = false;


    private int maxMessageSize = 131072;


    private int retryTimesWhenSendFailed = 2;


    private int retryTimesWhenSendAsyncFailed = 2;
    private DefaultMQProducer defaultMQProducer;
    private TransactionMQProducer transactionMQProducer;
    private Map<String, YiyiMqProduceMessageConfirm> yiyiMqProduceMessageConfirmMap;

    public DefaultMQProducer getDefaultMQProducer() {
        return this.defaultMQProducer;
    }

    public TransactionMQProducer getTransactionMQProducer() {
        return this.transactionMQProducer;
    }


    public DefaultMQProducer initDefaultMQProducer() throws MQClientException {
        log.info("init DefaultMQProducer start");
        this.defaultMQProducer = createDefaultMQProducer(new DefaultMQProducer(this.producerGroup));
        this.defaultMQProducer.start();
        log.info("init DefaultMQProducer success");
        return this.defaultMQProducer;
    }


    public TransactionMQProducer initTransactionMQProducer() throws MQClientException {
        log.info("init TransactionMQProducer start");
        this.transactionMQProducer = new TransactionMQProducer("tx-" + this.producerGroup);
        this.transactionMQProducer = createTransactionMQProducer(this.transactionMQProducer);
        this.transactionMQProducer.start();
        log.info("init TransactionMQProducer success");
        return this.transactionMQProducer;
    }

    private DefaultMQProducer createDefaultMQProducer(DefaultMQProducer defaultMQProducer) throws MQClientException {
        defaultMQProducer.setNamesrvAddr(this.namesServerAddress);
        defaultMQProducer.setInstanceName(this.producerGroup);
        defaultMQProducer.setRetryTimesWhenSendAsyncFailed(0);
        int clientCallbackExecutorThreads = 4;
        defaultMQProducer.setClientCallbackExecutorThreads(clientCallbackExecutorThreads);
        defaultMQProducer.setPersistConsumerOffsetInterval(this.persistConsumerOffsetInterval);
        defaultMQProducer.setSendMsgTimeout(this.sendMsgTimeout);
        defaultMQProducer.setCompressMsgBodyOverHowmuch(this.compressMsgBodyOverHowmuch);
        defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK(false);
        defaultMQProducer.setRetryTimesWhenSendFailed(this.retryTimesWhenSendFailed);
        defaultMQProducer.setRetryTimesWhenSendAsyncFailed(this.retryTimesWhenSendAsyncFailed);
        return defaultMQProducer;
    }

    private TransactionMQProducer createTransactionMQProducer(TransactionMQProducer transactionMQProducer) {
        transactionMQProducer.setNamesrvAddr(this.namesServerAddress);

        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000), new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("client-transaction-msg-check-thread");
                return thread;
            }
        });
        transactionMQProducer.setExecutorService(executorService);
        transactionMQProducer.setTransactionListener(new TransactionListener() {
            private ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<>();


            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                String topic = message.getTopic();
                String transactionId = message.getTransactionId();
                YiyiMqProduceMessageConfirm YiyiMqProduceMessageConfirm = (YiyiMqProduceMessageConfirm) YiyiMqProducter.this.yiyiMqProduceMessageConfirmMap.get(topic);
                if (YiyiMqProduceMessageConfirm != null) {
                    try {
                        YiyiMqProduceMessageConfirm.confirm(message);
                        this.localTrans.put(transactionId, Integer.valueOf(1));
                        return LocalTransactionState.COMMIT_MESSAGE;
                    } catch (Exception e) {
                        YiyiMqProducter.log.error("topic:{},??????????????????????????????,e:{}", topic, e);
                        this.localTrans.put(transactionId, Integer.valueOf(2));
                        return LocalTransactionState.ROLLBACK_MESSAGE;
                    }
                }
                YiyiMqProducter.log.info("topic:{},{}", topic, "?????????????????????");

                this.localTrans.put(transactionId, Integer.valueOf(1));

                return LocalTransactionState.UNKNOW;
            }


            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                String transactionId = messageExt.getTransactionId();
                YiyiMqProducter.log.info("check local transaction, topic:{},transactionId:{}", messageExt.getTopic(), transactionId);
                Integer status = this.localTrans.get(transactionId);
                if (null != status) {
                    int size;
                    switch (status.intValue()) {
                        case 0:
                            size = this.localTrans.size();
                            YiyiMqProducter.log.info("localTrans size:{}", Integer.valueOf(size));

                            if (size > 10000) {
                                this.localTrans.clear();
                            }
                            return LocalTransactionState.UNKNOW;
                        case 1:
                            this.localTrans.remove(transactionId);
                            return LocalTransactionState.COMMIT_MESSAGE;
                        case 2:
                            this.localTrans.remove(transactionId);
                            return LocalTransactionState.ROLLBACK_MESSAGE;
                    }
                    this.localTrans.remove(transactionId);
                    return LocalTransactionState.COMMIT_MESSAGE;
                }

                this.localTrans.remove(transactionId);
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });
        return transactionMQProducer;
    }


    public void close() throws IOException {
        if (this.defaultMQProducer != null) {
            this.defaultMQProducer.shutdown();
        }
        if (this.transactionMQProducer != null) {
            this.transactionMQProducer.shutdown();
        }
    }

    public void setNamesServerAddress(String namesServerAddress) {
        this.namesServerAddress = namesServerAddress;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public void setyiyiMqProduceMessageConfirmMap(Map<String, YiyiMqProduceMessageConfirm> yiyiMqProduceMessageConfirmMap) {
        this.yiyiMqProduceMessageConfirmMap = yiyiMqProduceMessageConfirmMap;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}