package com.yiyi.ribbon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/eat")
public class EatController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/fruit1")
    public String eatFruit(){
        return restTemplate.postForObject("http://eat/eat/fruit",null,String.class);
    }
}
