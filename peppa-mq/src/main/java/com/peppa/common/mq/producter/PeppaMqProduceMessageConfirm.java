package com.peppa.common.mq.producter;

import org.apache.rocketmq.common.message.Message;

public interface PeppaMqProduceMessageConfirm {
  void confirm(Message paramMessage);
}