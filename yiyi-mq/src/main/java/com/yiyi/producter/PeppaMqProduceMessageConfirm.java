package com.yiyi.producter;

import org.apache.rocketmq.common.message.Message;

public interface yiyiMqProduceMessageConfirm {
  void confirm(Message paramMessage);
}