package com.husky.intf;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 吃饭
 */
@FeignClient(name = "eat", path = "/eat")
public interface EatService {
    /**
     * 吃
     */
    @GetMapping("/apple")
    String eatApple();

    /**
     * 吃🍊
     */
    @GetMapping("/orange")
    String eatOrange(String who);
}
