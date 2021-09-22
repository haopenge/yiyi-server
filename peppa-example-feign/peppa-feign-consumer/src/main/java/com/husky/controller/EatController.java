package com.husky.controller;

import com.husky.intf.EatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eat")
public class EatController {

    @Autowired
    private EatService eatService;

    /**
     * 吃
     */
    @GetMapping("/apple")
    public String eatApple() {
        return "provider: " + eatService.eatApple();
    }

    @GetMapping("/orange/{who}")
    public String eatOrange(@PathVariable("who") String who) {
        return "provider: " + who + " eat orange";
    }
}
