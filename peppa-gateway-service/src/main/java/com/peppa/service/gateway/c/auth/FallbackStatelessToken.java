package com.peppa.service.gateway.c.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FallbackStatelessToken {
    private static final Logger logger = LoggerFactory.getLogger(FallbackStatelessToken.class);

    public static Integer getFallbackUserIdByToken(String token) {
        if (token == null || token.length() <= "fallback".length() || !token.startsWith("fallback")) {
            return null;
        }
        try {
            String decryptedToken = DesHelper.decrypt(token.substring("fallback".length()), "%*fallback%&!!(");
            if (decryptedToken.length() <= "FALLBACK-TOKEN-USER-ID-".length() || !decryptedToken.startsWith("FALLBACK-TOKEN-USER-ID-")) {
                return null;
            }
            return Integer.valueOf(decryptedToken.substring("FALLBACK-TOKEN-USER-ID-".length()));
        } catch (Exception e) {
            logger.warn(String.format("StatelessToken decrypt error,token:%s", token), e);

            return null;
        }
    }
}
