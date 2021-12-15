package com.husky.intf;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 吃饭
 */
@FeignClient(name = "yiyi-feign-provider", path = "/eat")
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
    String eatOrange(@RequestParam("who") String who);
}
