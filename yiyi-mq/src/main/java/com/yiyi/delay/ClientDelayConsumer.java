package com.yiyi.delay;

import org.apache.rocketmq.common.message.MessageExt;

public interface ClientDelayConsumer {
  void consume(MessageExt paramMessageExt);
}