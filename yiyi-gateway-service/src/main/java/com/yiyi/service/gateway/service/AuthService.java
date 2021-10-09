package com.yiyi.service.gateway.service;

import com.yiyi.common.response.Response;
import com.yiyi.service.gateway.b.auth.TokenUser;
import com.yiyi.service.gateway.b.auth.UimFeign;
import com.yiyi.service.gateway.c.auth.UserAuthFeign;
import com.yiyi.service.gateway.filter.AuthFilterConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;
import java.util.Set;


@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Value("#{'${gateway.c.auth.access_token.keys:user-token}'.split(',')}")
    private Set<String> userTokenKeys;

    @Value("#{'${gateway.c.auth.ignored_url_patterns:1}'.split(',')}")
    private Set<String> cAuthIgnoredUrlPatterns;

    @Value("#{'${gateway.c.auth.common_ignored_url_patterns:1}'.split(',')}")
    private Set<String> cAuthCommonIgnored;

    @Value("#{'${gateway.b.auth.access_token.keys:sso-token}'.split(',')}")
    private Set<String> accessTokenKeys;

    @Value("#{'${gateway.b.auth.ignored_url_patterns:1}'.split(',')}")
    private Set<String> bAuthIgnoredUrlPatterns;

    @Value("#{'${gateway.b.auth.common_ignored_url_patterns:1}'.split(',')}")
    private Set<String> bAuthCommonIgnored;

    @Autowired
    private UimFeign uimFeign;

    @Autowired
    private UserAuthFeign userAuthFeign;

    public boolean localVerifyIgnoreUrl(ServerHttpRequest request, AuthFilterConfig config) {
        String currentUri = request.getPath().toString();
        log.debug("current uri is : {}", currentUri);
        if (isBAuthIgnoredUrl(currentUri)) {
            return true;
        }
        if (config.getIgnoreAuthUrlStream().anyMatch(ignoreUrl -> PATH_MATCHER.match(ignoreUrl, currentUri))) {
            return true;
        }
        return false;
    }

    public boolean localVerifyToken(ServerWebExchange exchange, AuthFilterConfig config, String clientIdInRequest) {
        if (StringUtils.isNotBlank(clientIdInRequest) && config
                .getAllowedClientIds().count() > 0L && config
                .getAllowedClientIds().noneMatch(clientId -> clientId.equalsIgnoreCase(clientIdInRequest))) {
            return false;
        }
        TokenUser tokenUser = (TokenUser) exchange.getAttributes().getOrDefault("yiyi-token-user", null);
        if (tokenUser != null && config
                .getAllowedClientIds().count() > 0L && config
                .getAllowedClientIds().noneMatch(clientId -> clientId.equalsIgnoreCase(tokenUser.getClientId()))) {
            return false;
        }
        return true;
    }

    public Response verifyToken(String token, String accessUrl) {
        return this.uimFeign.verify(token, accessUrl);
    }

    public Response verifyTokenV2(String token, String accessUrl) {
        return this.uimFeign.verifyV2(token, accessUrl);
    }

    public Response verifyToken(String clientId, String token, String accessUrl) {
        return this.uimFeign.verifyForMicroApp(clientId, token, accessUrl);
    }

    public Response verifyTokenV2(String clientId, String token, String accessUrl) {
        return this.uimFeign.verifyForMicroAppV2(clientId, token, accessUrl);
    }

    public Response getUserIdByToken(String token) {
        return this.userAuthFeign.getUserIdByToken(token);
    }

    public boolean isBAuthIgnoredUrl(String url) {
        if (this.bAuthIgnoredUrlPatterns.stream().anyMatch(ignoreUrl -> PATH_MATCHER.match(ignoreUrl, url))) {
            return true;
        }
        if (this.bAuthCommonIgnored.stream().anyMatch(ignoreUrl -> PATH_MATCHER.match(ignoreUrl, url))) {
            return true;
        }
        return false;
    }

    public boolean isCAuthIgnoredUrl(String url) {
        if (this.cAuthIgnoredUrlPatterns.stream().anyMatch(ignoreUrl -> PATH_MATCHER.match(ignoreUrl, url))) {
            return true;
        }
        if (this.cAuthCommonIgnored.stream().anyMatch(ignoreUrl -> PATH_MATCHER.match(ignoreUrl, url))) {
            return true;
        }
        return false;
    }

    public String getAccessTokenFromRequest(ServerHttpRequest request) {
        return getTokenFromRequest(request, this.accessTokenKeys);
    }

    public String getUserTokenFromRequest(ServerHttpRequest request) {
        return getTokenFromRequest(request, this.userTokenKeys);
    }

    public Integer getUserIdFromRequest(ServerHttpRequest request) {
        if (request == null) {
            return 0;
        }
        HttpHeaders headers = request.getHeaders();
        if (headers.size() == 0) {
            return 0;
        }
        String token = null;
        for (String key : this.userTokenKeys) {
            if (StringUtils.isNotBlank(headers.getFirst(key))) {
                token = headers.getFirst(key);

                break;
            }
        }
        if (StringUtils.isEmpty(token) || "null".equals(token.trim()) || isMatch(token)) {
            return null;
        }
        Integer userId = getUserIdByTokenNative(token);
        if (isValidUserId(userId)) {
            return userId;
        }
        if (Objects.nonNull(userId) && Objects.equals(userId, -1)) {
            return -1;
        }
        return 0;
    }

    private Integer getUserIdByTokenNative(String token) {
        Response<Integer> userIdResponse = getUserIdByToken(token);
        if (userIdResponse.isSuccess())
            return userIdResponse.getData();
        userIdResponse.getCode();
        return null;
    }

    private boolean isMatch(String token) {
        return (token.contains("%3D") || token.contains("object") || token.length() < 30);
    }

    private boolean isValidUserId(Integer userId) {
        return (userId != null && userId > 0);
    }

    private String getTokenFromRequest(ServerHttpRequest request, Set<String> tokenKeys) {
        HttpHeaders headers = request.getHeaders();
        MultiValueMap<String, String> params = request.getQueryParams();
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        for (String tokenKey : this.accessTokenKeys) {
            String token = headers.getFirst(tokenKey);
            if (StringUtils.isNotBlank(token)) {
                return token;
            }
            token = params.getFirst(tokenKey);
            if (StringUtils.isNotBlank(token)) {
                return token;
            }
            HttpCookie tokenCookie = cookies.getFirst(tokenKey);
            if (tokenCookie != null && StringUtils.isNotBlank(tokenCookie.getValue())) {
                return tokenCookie.getValue();
            }
        }
        return "";
    }
}
