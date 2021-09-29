package com.peppa.service.gateway.filter;

import com.peppa.common.response.Response;
import com.peppa.service.gateway.service.AuthService;
import com.peppa.service.gateway.utils.ResponseUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Stream;

@Component
public class CAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<CAuthGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(CAuthGatewayFilterFactory.class);

    @Value("#{'${gateway.c.auth.access_token.keys:user-token}'.split(',')}")
    private Set<String> ACEESS_TOKEN_KEY;

    @Value("#{'${gateway.c.auth.ignored_url_patterns:1}'.split(',')}")
    private Set<String> ignoredUrlPatterns;

    @Value("#{'${gateway.c.auth.common_ignored_url_patterns:1}'.split(',')}")
    private Set<String> COMMON_IGNORED;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Resource
    private AuthService authService;

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("routeIgnoreAuthUrls");
    }

    public CAuthGatewayFilterFactory() {
        super(CAuthGatewayFilterFactory.Config.class);
    }

    public GatewayFilter apply(CAuthGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String currentUri = request.getPath().toString();
            log.debug("current uri is : {}", currentUri);
            if (this.COMMON_IGNORED.stream().anyMatch((ignoreUrl) -> {
                return PATH_MATCHER.match(ignoreUrl, currentUri);
            })) {
                return chain.filter(exchange);
            } else if (this.ignoredUrlPatterns.stream().anyMatch((ignoreUrl) -> {
                return PATH_MATCHER.match(ignoreUrl, currentUri);
            })) {
                return chain.filter(exchange);
            } else if (config.getIgnoreAuthUrlStream().anyMatch((ignoreUrl) -> {
                return PATH_MATCHER.match(ignoreUrl, currentUri);
            })) {
                return chain.filter(exchange);
            } else {
                try {
                    Integer userId = this.getUserIdFromRequest(request);
                    if (this.isValidUserId(userId)) {
                        return chain.filter(exchange);
                    } else {
                        Response response;
                        if (userId != null && userId == -1) {
                            response = (new Response()).failure(CAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_INVALID.message).code(CAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_INVALID.code);
                        } else {
                            response = (new Response()).failure(CAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_UNAUTHORIZED.message).code(CAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_UNAUTHORIZED.code);
                        }

                        ServerHttpResponse httpResponse = exchange.getResponse();
                        httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                        DataBuffer buffer = ResponseUtils.getResponeBuffer(httpResponse, response);
                        return httpResponse.writeWith(Mono.just(buffer));
                    }
                } catch (Exception var10) {
                    log.error("gateway error on cauth : {}", var10.getMessage());
                    var10.printStackTrace();
                    return chain.filter(exchange);
                }
            }
        };
    }

    public String getSsoToken(ServerHttpRequest request) {
        if (request == null) {
            return "";
        } else {
            HttpHeaders headers = request.getHeaders();
            return headers.size() != 0 ? headers.getFirst("sso-token") : "";
        }
    }

    public Integer getUserIdFromRequest(ServerHttpRequest request) {
        if (request == null) {
            return 0;
        } else {
            HttpHeaders headers = request.getHeaders();
            if (headers.size() != 0) {
                String token = null;
                Iterator var4 = this.ACEESS_TOKEN_KEY.iterator();

                while(var4.hasNext()) {
                    String key = (String)var4.next();
                    if (StringUtils.isNotBlank(headers.getFirst(key))) {
                        token = headers.getFirst(key);
                        break;
                    }
                }

                if (!StringUtils.isEmpty(token) && !"null".equals(token.trim()) && !this.isMatch(token)) {
                    Integer userId = this.getUserIdByToken(token);
                    if (this.isValidUserId(userId)) {
                        return userId;
                    } else {
                        return Objects.nonNull(userId) && Objects.equals(userId, -1) ? -1 : 0;
                    }
                } else {
                    return null;
                }
            } else {
                return 0;
            }
        }
    }

    public Integer getUserIdByToken(String token) {
        Response<Integer> userIdResponse = this.authService.getUserIdByToken(token);
        if (userIdResponse.isSuccess()) {
            return (Integer)userIdResponse.getData();
        } else {
            return "4000".equals(userIdResponse.getCode()) && Objects.nonNull(userIdResponse.getData()) && Objects.equals(userIdResponse.getData(), -1) ? -1 : null;
        }
    }

    private boolean isMatch(String token) {
        return token.contains("%3D") || token.contains("object") || token.length() < 30;
    }

    private boolean isValidUserId(Integer userId) {
        return userId != null && userId > 0;
    }

    static enum UserAuthExceptionEnum {
        USER_UNAUTHORIZED(100003, "用户未授权"),
        USER_INVALID(100004, "用户已失效");

        private Integer code;
        private String message;

        private UserAuthExceptionEnum(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static class Config {
        private String routeIgnoreAuthUrls;

        public Stream<String> getIgnoreAuthUrlStream() {
            return StringUtils.isBlank(this.routeIgnoreAuthUrls) ? Stream.empty() : Stream.of(this.routeIgnoreAuthUrls.split("\\|"));
        }

        public Config() {
        }

        public String getRouteIgnoreAuthUrls() {
            return this.routeIgnoreAuthUrls;
        }

        public void setRouteIgnoreAuthUrls(String routeIgnoreAuthUrls) {
            this.routeIgnoreAuthUrls = routeIgnoreAuthUrls;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof CAuthGatewayFilterFactory.Config)) {
                return false;
            } else {
                CAuthGatewayFilterFactory.Config other = (CAuthGatewayFilterFactory.Config)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$routeIgnoreAuthUrls = this.getRouteIgnoreAuthUrls();
                    Object other$routeIgnoreAuthUrls = other.getRouteIgnoreAuthUrls();
                    if (this$routeIgnoreAuthUrls == null) {
                        if (other$routeIgnoreAuthUrls != null) {
                            return false;
                        }
                    } else if (!this$routeIgnoreAuthUrls.equals(other$routeIgnoreAuthUrls)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof CAuthGatewayFilterFactory.Config;
        }

        public int hashCode() {
            String routeIgnoreAuthUrls = this.getRouteIgnoreAuthUrls();
            return 59 + (routeIgnoreAuthUrls == null ? 43 : routeIgnoreAuthUrls.hashCode());
        }

        public String toString() {
            return "CAuthGatewayFilterFactory.Config(routeIgnoreAuthUrls=" + this.getRouteIgnoreAuthUrls() + ")";
        }
    }
}
