package com.controller;


import com.mq.MqTopicConstant;
import com.peppa.common.mq.producter.PeppaMqProduceMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class GrayTestController {

    @Autowired
    private PeppaMqProduceMessage message;


    @GetMapping("/test")
    public String hello() {
        try {
            message.sendMessage(MqTopicConstant.SLEEP, "睡觉咯，睡觉咯，yiyi");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }
}
