package com.peppa.service.gateway.c.auth;

import com.peppa.common.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "PEPPA-USER-AUTH-SERVER", path = "/UserAuthService", fallback = StableUserAuthFeignFallback.class)
public interface UserAuthFeign {
    @PostMapping({"/getUserIdByToken"})
    Response<Integer> getUserIdByToken(@RequestParam("token") String paramString);
}