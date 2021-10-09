package com.yiyi.service.gateway.auth;

import com.yiyi.common.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "yiyi-USER-AUTH-SERVER", path = "/UserAuthService", fallback = StableUserAuthFeignFallback.class)
public interface UserAuthFeign {
    @PostMapping({"/getUserIdByToken"})
    Response<Integer> getUserIdByToken(@RequestParam("token") String paramString);
}