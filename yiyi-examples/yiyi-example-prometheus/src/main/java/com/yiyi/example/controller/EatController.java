package com.yiyi.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/eat")
@RestController
public class EatController {

    @PostMapping("/apple")
    public String eatApple(){
        return "eat apple";
    }
}
