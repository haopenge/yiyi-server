package com.yiyi.service.gateway.filter;

import com.yiyi.service.gateway.b.auth.TokenDecoder;
import com.yiyi.service.gateway.b.auth.TokenUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Set;

@Component
public class RequestTokenUserFilter
        implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestTokenUserFilter.class);


    @Value("#{'${gateway.b.auth.access_token.keys:1}'.split(',')}")
    private Set<String> ACEESS_TOKEN_KEY;


    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        TokenUser tokenUser = generateTokenUser(getTokenFromRequest(request));
        if (tokenUser == null) {
            return chain.filter(exchange);
        }
        exchange.getAttributes().put("yiyi-token-user", tokenUser);

        try {
            URI uri = exchange.getRequest().getURI();
            StringBuilder query = new StringBuilder();
            String originalQuery = uri.getRawQuery();
            if (StringUtils.isNotEmpty(originalQuery)) {
                query.append(originalQuery);
                if (originalQuery.charAt(originalQuery.length() - 1) != '&') {
                    query.append('&');
                }
            }
            query.append("__gw_trans_u_").append("=").append(generateUserString(tokenUser));
            URI newUri = UriComponentsBuilder.fromUri(uri).replaceQuery(query.toString()).build(true).toUri();
            request = exchange.getRequest().mutate().uri(newUri).build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return chain.filter(exchange);
        }
    }

    private String generateUserString(TokenUser tokenUser) {
        if (tokenUser == null) {
            return "";
        }
        String[] arr = {"" + tokenUser.getEmployeeId(), "" + tokenUser.getAccountId(), tokenUser.getName(), tokenUser.getUsername()};
        StringBuilder uinfo = new StringBuilder(StringUtils.join((Object[]) arr, ","));
        try {
            return URLEncoder.encode(Base64.getEncoder().encodeToString(uinfo.toString().getBytes()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

            return "";
        }
    }

    private TokenUser generateTokenUser(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        TokenUser tokenUser = TokenDecoder.decodeToken(accessToken);
        if (tokenUser == null) {
            log.warn("could not extract userinfo from access_token {}", accessToken);
        }
        return tokenUser;
    }


    private String getTokenFromRequest(ServerHttpRequest request) {
        String ssoAccessToken = null;
        for (String tokenName : this.ACEESS_TOKEN_KEY) {
            ssoAccessToken = request.getHeaders().getFirst(tokenName);
            if (StringUtils.isBlank(ssoAccessToken)) {
                ssoAccessToken = (String) request.getQueryParams().getFirst(tokenName);
            }

            if (StringUtils.isNotBlank(ssoAccessToken)) {
                return ssoAccessToken;
            }
        }

        if (StringUtils.isBlank(ssoAccessToken)) {
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            if (cookies == null || cookies.size() == 0) {
                return ssoAccessToken;
            }
            if (!cookies.containsKey("yiyi_sso_token")) {
                return ssoAccessToken;
            }
            ssoAccessToken = ((HttpCookie) cookies.getFirst("yiyi_sso_token")).getValue();
        }
        return ssoAccessToken;
    }


    public int getOrder() {
        return -2147483647;
    }
}
