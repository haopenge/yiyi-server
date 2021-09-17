package com.controller;


import com.husky.intf.GrayTestService;
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

    @Autowired
    private GrayTestService grayTestService;


    @GetMapping("/test")
    public String hello() {
        try {
            message.sendMessage(MqTopicConstant.EAT, "吃饭了，吃饭了，yiyi");
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @GetMapping("/eat_after")
    public String eatAfter(String who) {
        grayTestService.eatAfter(who);
        return "success";
    }


}
