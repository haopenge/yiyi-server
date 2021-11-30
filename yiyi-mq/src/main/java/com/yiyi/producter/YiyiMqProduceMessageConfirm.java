package com.yiyi.producter;

import org.apache.rocketmq.common.message.Message;

public interface YiyiMqProduceMessageConfirm {
  void confirm(Message paramMessage);
}