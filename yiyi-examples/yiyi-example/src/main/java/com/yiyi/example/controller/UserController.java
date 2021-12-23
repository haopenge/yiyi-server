package com.yiyi.example.controller;

import com.yi.auth.service.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private MsgService msgService;

    @PostMapping("/send_msg")
    public String sendMsg(){
        msgService.sendMessageOnXxPlatform("code_12138","16602223926","1","2","3");
        return "send msg success";
    }

    @GetMapping("/login")
    public String eatOrange() {
        return "login success";
    }


    @GetMapping("/send_code")
    public String sendCode() {
        return "send_code success";
    }
}
