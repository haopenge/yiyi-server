/*    */
package com.yiyi.service.gateway.auth;

import com.yiyi.common.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
public class UimFeignFallback implements UimFeign {
    private static final Logger log = LoggerFactory.getLogger(UimFeignFallback.class);


    public Response verify(String accessToken, String accessUrl) {
        log.warn("fallback in execute uimfeign");
        if (StringUtils.isBlank(accessToken)) {
            return (new FallbackResponse()).failure().code(HttpStatus.UNAUTHORIZED.value()).message("invalid access_token : " + accessToken);
        }
        TokenUser tokenUser = TokenDecoder.decodeToken(accessToken);
        if (tokenUser == null) {
            return (new FallbackResponse()).failure().code(HttpStatus.UNAUTHORIZED.value()).message("invalid access_token : " + accessToken);
        }
        return (new FallbackResponse()).success().code(HttpStatus.OK.value()).message(HttpStatus.OK.getReasonPhrase());
    }


    public Response verifyV2(String accessToken, String accessUrl) {
        return verify(accessToken, accessUrl);
    }


    public Response verifyForMicroApp(String clientId, String accessToken, String accessUrl) {
        return verify(accessToken, accessUrl);
    }


    public Response verifyForMicroAppV2(String clientId, String accessToken, String accessUrl) {
        return verify(accessToken, accessUrl);
    }


    public Response<TokenUser> verifyAndGetTokenUser(String accessToken, String accessUrl) {
        log.warn("fallback in execute uimfeign verifyAndGetTokenUser");
        if (StringUtils.isBlank(accessToken)) {
            return (new FallbackResponse()).failure().code(HttpStatus.UNAUTHORIZED.value()).message("invalid access_token : " + accessToken);
        }
        TokenUser tokenUser = TokenDecoder.decodeToken(accessToken);
        if (tokenUser == null) {
            return (new FallbackResponse()).failure().code(HttpStatus.UNAUTHORIZED.value()).message("invalid access_token : " + accessToken);
        }
        return (new FallbackResponse()).success().data(tokenUser).code(HttpStatus.OK.value()).message(HttpStatus.OK.getReasonPhrase());
    }


    public Response<TokenUser> verifyAndGetTokenUserV2(String accessToken, String accessUrl) {
        return verify(accessToken, accessUrl);
    }
}
