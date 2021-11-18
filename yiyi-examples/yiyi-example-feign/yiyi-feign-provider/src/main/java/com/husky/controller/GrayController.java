package com.husky.controller;

import com.husky.mq.MqTopicConstant;
import com.yiyi.common.mq.producter.yiyiMqProduceMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@RestController
@RequestMapping("/gray")
public class GrayController {

    @Autowired
    private yiyiMqProduceMessage message;

    /**
     * 饭后操作
     */
    @GetMapping("/eat/after")
    public String eatAfter() {
        try {
            System.out.println("==========================》 哎呀呀，我被调用了");
            message.sendMessage(MqTopicConstant.EAT_AFTER, "name:" + "yiyi" + "  .....  吃完饭 要刷牙哦");
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }
}
