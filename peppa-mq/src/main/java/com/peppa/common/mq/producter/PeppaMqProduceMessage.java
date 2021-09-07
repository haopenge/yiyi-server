package com.peppa.common.mq.producter;

import com.peppa.common.mq.acc.MqTrace;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@Slf4j
public class PeppaMqProduceMessage {
    private DefaultMQProducer defaultMQProducer;

    private TransactionMQProducer transactionMQProducer;

    private MqTrace mqTrace;

    private boolean isGray = false;

    private String serverPodEnv;

    public PeppaMqProduceMessage() {
        log.info("Peppa mq PeppaMqProduceMessage init!");
        String isgraystr = System.getProperty("mq_gray");
        if ("enable_gray".equals(isgraystr)) {
            this.isGray = true;
            this.serverPodEnv = System.getProperty("podenv");
            if (this.serverPodEnv == null || this.serverPodEnv.length() == 0) {
                this.serverPodEnv = System.getenv("podenv");
            }
            if (this.serverPodEnv == null) {
                this.serverPodEnv = "";
            }
            log.info("MQ 生产者灰度环境生效：{}!", this.serverPodEnv);
        }
    }

    public void sendAsyncMessage(String topic, String msg, SendCallback sendCallback) throws Exception {
        Message message = new Message(topic, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        this.defaultMQProducer.send(message, sendCallback);
    }

    public void sendAsyncMessage(String topic, String tags, String msg, SendCallback sendCallback) throws Exception {
        Message message = new Message(topic, tags, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        this.defaultMQProducer.send(message, sendCallback);
    }

    public void sendAsyncMessage(String topic, String tags, String keys, String msg, SendCallback sendCallback) throws Exception {
        Message message = new Message(topic, tags, keys, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        this.defaultMQProducer.send(message, sendCallback);
    }

    public void sendMessage(String topic, String msg) throws Exception {
        Message message = new Message(topic, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        SendResult send = this.defaultMQProducer.send(message);
        send.getSendStatus();
    }

    public void sendMessageByKey(String topic, String msg, String keys) throws Exception {
        Message message = new Message(topic, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        SendResult send = this.defaultMQProducer.send(message);
        send.getSendStatus();
    }

    public void sendMessage(String topic, String tags, String msg) throws Exception {
        Message message = new Message(topic, tags, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        SendResult send = this.defaultMQProducer.send(message);
        send.getSendStatus();
    }

    public void sendMessage(String topic, String tags, String keys, String msg) throws Exception {
        Message message = new Message(topic, tags, keys, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        SendResult send = this.defaultMQProducer.send(message);
        send.getSendStatus();
    }

    public void sendDelayMessage(String topic, String msg, int delayTime) throws Exception {
        Message message = new Message(topic, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        message.setDelayTimeLevel(delayTime);
        SendResult send = this.defaultMQProducer.send(message);
        send.getSendStatus();
    }


    public void sendOrderMessage(String topic, String msg, String key) throws Exception {
        Message message = new Message(topic, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        this.defaultMQProducer.send(message, (MessageQueueSelector) new SelectMessageQueueByHash(), key);
    }


    public void sendOrderDelayMessage(String topic, String msg, String key, int delayTime) throws Exception {
        Message message = new Message(topic, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        message.setDelayTimeLevel(delayTime);
        this.defaultMQProducer.send(message, (MessageQueueSelector) new SelectMessageQueueByHash(), key);
    }

    public void sendOrderMessage(String topic, String tags, String msg, String key) throws Exception {
        Message message = new Message(topic, tags, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        this.defaultMQProducer.send(message, (MessageQueueSelector) new SelectMessageQueueByHash(), key);
    }

    public void sendOrderDelayMessage(String topic, String tags, String msg, String key, int delayTime) throws Exception {
        Message message = new Message(topic, tags, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        message.setDelayTimeLevel(delayTime);
        dealMessageGray(message);
        this.defaultMQProducer.send(message, (MessageQueueSelector) new SelectMessageQueueByHash(), key);
    }

    public void sendOrderMessage(String topic, String tags, String keys, String msg, String key) throws Exception {
        Message message = new Message(topic, tags, keys, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        this.defaultMQProducer.send(message, (MessageQueueSelector) new SelectMessageQueueByHash(), key);
    }

    public void sendTransactionMessage(String topic, String msg) throws Exception {
        Message message = new Message(topic, msg.getBytes(StandardCharsets.UTF_8));
        if (this.mqTrace != null) {
            String k = this.mqTrace.productPrintLog();
            if (k != null) {
                message.setKeys(this.mqTrace.productPrintLog());
            }
        }
        dealMessageGray(message);
        this.transactionMQProducer.sendMessageInTransaction(message, null);
    }

    public void setDefaultMQProducer(DefaultMQProducer defaultMQProducer) {
        this.defaultMQProducer = defaultMQProducer;
    }

    public DefaultMQProducer getDefaultMQProducer() {
        return this.defaultMQProducer;
    }

    public void setTransactionMQProducer(TransactionMQProducer transactionMQProducer) {
        this.transactionMQProducer = transactionMQProducer;
    }

    public MqTrace getMqTrace() {
        return this.mqTrace;
    }

    public void setMqTrace(MqTrace mqTrace) {
        this.mqTrace = mqTrace;
    }

    private void dealMessageGray(Message message) {
        if (!this.isGray) {
            return;
        }
        boolean isThreadlocalSet = false;

        try {
            Class<?> threadlocalClazz = Class.forName("com.peppa.grayconfig.ThreadAttributes");
            Method method = threadlocalClazz.getMethod("getHeaderValue", String.class);
            String headerpodenv = (String) method.invoke(null, "podenv");

            if (headerpodenv != null && !headerpodenv.equals("qa") && !headerpodenv.equals("dev") && !headerpodenv.equals("sim")) {
                isThreadlocalSet = true;
                message.putUserProperty("podenv", headerpodenv);
                log.info("MQ生成者生成消息，独立环境{}请求，消息加属性", headerpodenv);
            }
        } catch (Exception e) {
            log.warn("MQ生成者{}没有找到，没有引用架构组件！微服务环境必须引用！", "com.peppa.common.grayconfig.ThreadAttributes");
        }

        if (!isThreadlocalSet && this.serverPodEnv != null && this.serverPodEnv.trim().length() > 0)
            message.putUserProperty("podenv", this.serverPodEnv);
    }
}
