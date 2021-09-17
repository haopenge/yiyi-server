package com.husky.intf.controller;

import com.husky.intf.mq.MqTopicConstant;
import com.peppa.common.mq.producter.PeppaMqProduceMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 */
@RestController
@RequestMapping("/gray")
public class GrayController {

    @Autowired
    private PeppaMqProduceMessage message;
    
    /**
     * 饭后操作
     */
    @PostMapping("/eat/after")
    public String eatAfter(String who){
        try {
            message.sendMessage(MqTopicConstant.EAT_AFTER,"name:" + who + "  .....  吃完饭 要刷牙哦");
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }
}
