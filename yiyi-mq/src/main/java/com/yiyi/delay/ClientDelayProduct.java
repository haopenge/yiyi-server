package com.yiyi.delay;

import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class ClientDelayProduct {
    private static final Logger log = LoggerFactory.getLogger(ClientDelayProduct.class);
    public static ConcurrentHashMap<String, ArrayBlockingQueue<MessageExt>> map = new ConcurrentHashMap<>();

    public static ArrayBlockingQueue<MessageExt> getGroupQueue(String consumerGroup) {
        ArrayBlockingQueue<MessageExt> messageExts = map.get(consumerGroup);
        if (messageExts == null) {
            synchronized (ClientDelayProduct.class) {
                messageExts = new ArrayBlockingQueue<>(1);
                map.put(consumerGroup, messageExts);
            }
        }
        return messageExts;
    }


    public static void putMessage(String consumerGroup, MessageExt messageExt) {
        ArrayBlockingQueue<MessageExt> groupQueue = getGroupQueue(consumerGroup);
        try {
            groupQueue.put(messageExt);
        } catch (InterruptedException e) {
            log.error("客户端delay异常，e:", e);
            throw new RuntimeException("客户端delay异常");
        }
    }
}