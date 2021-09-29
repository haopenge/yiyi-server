package com.peppa.service.gateway.c.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service
public class StableUserService {
    public Integer getUserIdByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return FallbackStatelessToken.getFallbackUserIdByToken(token);
    }
}

