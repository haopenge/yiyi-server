package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

    @GetMapping("/login")
    public String eatOrange() {
        return "login success";
    }


    @GetMapping("/send_code")
    public String sendCode() {
        return "send_code success";
    }
}
