package com.yiyi.service.gateway.auth;

import com.yiyi.common.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StableUserAuthFeignFallback implements UserAuthFeign {
    private static final Logger log = LoggerFactory.getLogger(StableUserAuthFeignFallback.class);


    @Autowired
    private StableUserService stableUserService;


    public Response getUserIdByToken(String token) {
        log.warn("fallback in get user id by token");
        return FallbackResponse.getInstance().success().data(this.stableUserService.getUserIdByToken(token));
    }
}

