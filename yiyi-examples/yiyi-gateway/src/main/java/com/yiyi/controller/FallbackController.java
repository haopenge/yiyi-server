package com.yiyi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    private Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/fallbackA")
    public String fallbackA() {
        logger.warn("FallibackController.fallbackA fire fuse");
        return "fuse data";
    }
}
