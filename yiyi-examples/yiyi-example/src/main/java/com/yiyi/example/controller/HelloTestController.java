package com.yiyi.example.controller;

import org.springframework.web.bind.annotation.*;

/**
 *
 */
@RestController
@RequestMapping("/eat")
public class HelloTestController {
    /**
     * 吃
     */
    @GetMapping("/apple")
    public String eatApple() {
        return " 我吃了 ";
    }

    @GetMapping("/orange/{who}")
    public String eatOrange(@PathVariable("who") String who) {
        return who + " eat orange";
    }

    @GetMapping("/header")
    public String getHeader(@RequestHeader(name = "token") String token){
        return "token  = " + token;
    }
}
