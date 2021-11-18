package com.husky.controller;

import org.springframework.web.bind.annotation.GetMapping;
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
        System.out.println("-------》哎呀呀  我被调用了  com.husky.controller.EatController.eatApple");
        return " 我吃了 ";
    }

    @GetMapping("/orange")
    public String eatOrange(String who) {
        System.out.println("-------》哎呀呀  我被调用了  com.husky.controller.EatController.eatOrange");
        return who + " eat orange";
    }
}
