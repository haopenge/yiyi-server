package com.peppa.service.gateway.b.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;

import java.util.Map;


public class TokenDecoder {
    private static final Logger log = LoggerFactory.getLogger(TokenDecoder.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static TokenUser decodeToken(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        Map<String, Object> claims = null;
        try {
            Jwt jwt = JwtHelper.decode(accessToken);
            String claimsStr = jwt.getClaims();
            claims = (Map<String, Object>) objectMapper.readValue(claimsStr, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (claims == null) {
            return null;
        }
        TokenUser tokenUser = new TokenUser();
        tokenUser.setClientId(getStringFromMap(claims, "client_id"));
        tokenUser.setName(getStringFromMap(claims, "name"));
        tokenUser.setUsername(getStringFromMap(claims, "user_name"));
        tokenUser.setEmployeeId(getIntFromMap(claims, "user_id"));
        tokenUser.setAccountId(getIntFromMap(claims, "account_id"));
        return tokenUser;
    }

    private static String getStringFromMap(Map<String, Object> map, String key) {
        return map.getOrDefault(key, "").toString();
    }

    private static Integer getIntFromMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return 0;
        }
        try {
            return Integer.parseInt(map.get(key).toString());
        } catch (Exception e) {
            e.printStackTrace();

            return 0;
        }
    }
}

