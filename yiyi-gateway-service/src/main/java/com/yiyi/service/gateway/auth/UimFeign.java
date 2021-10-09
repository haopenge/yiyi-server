package com.yiyi.service.gateway.auth;

import com.yiyi.common.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "yiyi-UIM-SERVER", path = "/uim", fallback = UimFeignFallback.class)
public interface UimFeign {
    @RequestMapping(value = {"/verify"}, method = {RequestMethod.POST})
    Response verify(@RequestParam(name = "access_token") String paramString1, @RequestParam(name = "access_url", required = false) String paramString2);

    @RequestMapping(value = {"/verify_v2"}, method = {RequestMethod.POST})
    Response verifyV2(@RequestParam(name = "access_token") String paramString1, @RequestParam(name = "access_url", required = false) String paramString2);

    @RequestMapping(value = {"/verify"}, method = {RequestMethod.POST})
    Response verifyForMicroApp(@RequestHeader(name = "sso-client-id") String paramString1, @RequestParam(name = "access_token") String paramString2, @RequestParam(name = "access_url", required = false) String paramString3);

    @RequestMapping(value = {"/verify_v2"}, method = {RequestMethod.POST})
    Response verifyForMicroAppV2(@RequestHeader(name = "sso-client-id") String paramString1, @RequestParam(name = "access_token") String paramString2, @RequestParam(name = "access_url", required = false) String paramString3);

    @RequestMapping(value = {"/verify_and_get_user"}, method = {RequestMethod.POST})
    Response<TokenUser> verifyAndGetTokenUser(@RequestParam(name = "access_token") String paramString1, @RequestParam(name = "access_url", required = false) String paramString2);

    @RequestMapping(value = {"/verify_and_get_user_v2"}, method = {RequestMethod.POST})
    Response<TokenUser> verifyAndGetTokenUserV2(@RequestParam(name = "access_token") String paramString1, @RequestParam(name = "access_url", required = false) String paramString2);
}
