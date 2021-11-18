package com.yiyi.example.controller;


import com.husky.intf.GrayTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class GrayTestController {

    @Autowired
    private GrayTestService grayTestService;

    @GetMapping("/eat_after")
    public String eatAfter() {
        grayTestService.eatAfter();
        return "success";
    }


}
