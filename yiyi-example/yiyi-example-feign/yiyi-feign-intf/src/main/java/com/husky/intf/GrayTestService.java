package com.husky.intf;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 灰度Feign 服务测试
 */
@FeignClient(name = "yiyi-FEIGN-PROVIDER", path = "/gray")
public interface GrayTestService {
    /**
     * 饭后操作
     */
    @GetMapping(value = "/eat/after")
    void eatAfter();
}
