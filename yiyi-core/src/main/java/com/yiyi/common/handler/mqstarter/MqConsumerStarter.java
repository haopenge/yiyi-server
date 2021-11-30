package com.yiyi.common.handler.mqstarter;


import com.yiyi.consumer.YiyiMqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class MqConsumerStarter implements ApplicationListener {
    private static final Logger logger = LoggerFactory.getLogger(MqConsumerStarter.class);
    private AtomicBoolean isstart = new AtomicBoolean(false);


    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent &&
                this.isstart.compareAndSet(false, true))
            try {
                YiyiMqConsumer yiyiMqConsumer = (YiyiMqConsumer) ((ApplicationReadyEvent) event).getApplicationContext().getBean(YiyiMqConsumer.class);
                if (yiyiMqConsumer != null) {
                    try {
                        Method method = YiyiMqConsumer.class.getMethod("start", new Class[0]);
                        if (method == null) {
                            logger.error("yiyiMqConsumer start方法不存在，mqclient 需要升级!!!");
                        } else {
                            yiyiMqConsumer.start();
                        }
                    } catch (Exception e) {
                        logger.error("yiyiMqConsumer 启动失败：", e);
                    }
                }
            } catch (Throwable e) {
                logger.info("yiyiMqConsumer not exists.");
            }
    }
}

