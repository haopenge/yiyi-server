package com.controller;

import org.springframework.web.bind.annotation.*;

/**
 *
 */
@RestController
@RequestMapping("/eat")
public class FeignTestController {
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
}
