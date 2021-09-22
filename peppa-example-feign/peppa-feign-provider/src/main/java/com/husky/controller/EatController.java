package com.husky.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
@RequestMapping("/eat")
public class EatController {

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
