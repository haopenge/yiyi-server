package com.peppa;

import com.peppa.common.mq.producter.PeppaMqProduceMessage;
import com.peppa.grayconfig.ThreadAttributes;
import com.peppa.use.MqTopicConstant;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PeppaMqApplicationTests {

    @Autowired
    private PeppaMqProduceMessage message;

    @Test
    public void sendMessage() {
        ThreadAttributes.setThreadAttribute("huohua-podenv", "yiyi-1");

        try {
            message.sendMessage(MqTopicConstant.EAT, "太阳晒屁股了，起床了，骑车出去买酱油，打了酱油炒菜吃，吃完饭刷刷锅，刷完锅出去玩，噜啦噜啦噜啦啦 ，11");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
