package com.yiyi.ribbon.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eat")
public class EatController {

    @Value("${server.port}")
    private Integer port;


    @PostMapping("/fruit")
    public String eatFruit(){
        return " I eat fruit on  " + port;
    }
}
