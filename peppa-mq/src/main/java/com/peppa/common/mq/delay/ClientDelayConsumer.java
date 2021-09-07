package com.peppa.common.mq.delay;

import org.apache.rocketmq.common.message.MessageExt;

public interface ClientDelayConsumer {
  void consume(MessageExt paramMessageExt);
}