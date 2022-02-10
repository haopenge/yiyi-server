package com.yiyi.ribbon.controller;

import com.alibaba.fastjson.JSON;
import com.husky.intf.EatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eat")
public class EatController {

    @Value("${server.port}")
    private Integer port;

    @Autowired
    private EatService eatService;

    @PostMapping("/fruit")
    public String eatFruit(){
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("eat"," I eat fruit on  " + port);
        bodyMap.put("provider",eatService.eatApple());
        return JSON.toJSONString(bodyMap) ;
    }

}
