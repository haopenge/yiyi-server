package com.husky.intf;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 灰度Feign 服务测试
 */
@FeignClient(name = "GRAY-TEST", path = "/gray")
public interface GrayTestService {
    /**
     * 饭后操作
     */
    @PostMapping("/eat/after")
    void eatAfter(String who);
}
