package com;

import com.mq.MqTopicConstant;
import com.peppa.common.grayconfig.ThreadAttributes;
import com.peppa.common.mq.producter.PeppaMqProduceMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PeppaExampleApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PeppaExampleApplication.class, args);
        sendMessage(context);
    }

    public static void sendMessage(ConfigurableApplicationContext context) {
        PeppaMqProduceMessage message = context.getBean(PeppaMqProduceMessage.class);

        ThreadAttributes.setThreadAttribute("huohua-podenv", "yiyi-1");

        try {
            message.sendMessage(MqTopicConstant.SLEEP, "睡觉啦睡觉啦，噜啦噜啦噜啦啦 ，11");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
