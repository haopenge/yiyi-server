/*    */
package com.yiyi.delay;

import org.apache.rocketmq.common.message.MessageExt;

import java.util.concurrent.ArrayBlockingQueue;


public class ClientDelayUtil {
    public static void consumeClientDelayMessage(String consumerGroup, ClientDelayConsumer clientDelayConsumer, long delayTime) {
        ArrayBlockingQueue<MessageExt> groupQueue = ClientDelayProduct.getGroupQueue(consumerGroup);
        while (true) {
            MessageExt peek = groupQueue.peek();
            if (peek == null) {
                try {
                    Thread.sleep(200L);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            assert peek != null;
            long bornTimestamp = peek.getBornTimestamp();
            long now = System.currentTimeMillis();
            long interval = now - bornTimestamp;
            if (interval < delayTime) {
                try {
                    Thread.sleep(delayTime - interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            MessageExt poll = groupQueue.poll();
            clientDelayConsumer.consume(poll);
        }
    }
}
