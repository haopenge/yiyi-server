package com.peppa.common.handler.mqstarter;


import com.peppa.common.mq.consumer.PeppaMqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class MqConsumerStarter
        implements ApplicationListener {
    private static final Logger logger = LoggerFactory.getLogger(MqConsumerStarter.class);
    private AtomicBoolean isstart = new AtomicBoolean(false);


    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent &&
                this.isstart.compareAndSet(false, true))
            try {
                PeppaMqConsumer peppaMqConsumer = (PeppaMqConsumer) ((ApplicationReadyEvent) event).getApplicationContext().getBean(PeppaMqConsumer.class);
                if (peppaMqConsumer != null) {
                    try {
                        Method method = PeppaMqConsumer.class.getMethod("start", new Class[0]);
                        if (method == null) {
                            logger.error("PeppaMqConsumer start方法不存在，mqclient 需要升级!!!");
                        } else {
                            peppaMqConsumer.start();
                        }
                    } catch (Exception e) {
                        logger.error("peppaMqConsumer 启动失败：", e);
                    }
                }
            } catch (Throwable e) {
                logger.info("PeppaMqConsumer not exists.");
            }
    }
}

