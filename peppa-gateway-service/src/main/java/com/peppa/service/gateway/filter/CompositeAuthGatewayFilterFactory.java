package com.peppa.service.gateway.filter;

import com.peppa.common.response.Response;
import com.peppa.service.gateway.service.AuthService;
import com.peppa.service.gateway.utils.ResponseUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Component
public class CompositeAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<CompositeAuthGatewayFilterFactory.Config> {
    private static final Logger log = LoggerFactory.getLogger(CompositeAuthGatewayFilterFactory.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Resource
    private AuthService authService;

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("routeIgnoreAuthUrls");
    }

    public CompositeAuthGatewayFilterFactory() {
        super(CompositeAuthGatewayFilterFactory.Config.class);
    }

    public GatewayFilter apply(CompositeAuthGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String currentUri = request.getPath().toString();
            log.debug("current uri is : {}", currentUri);
            if (this.authService.isCAuthIgnoredUrl(currentUri)) {
                return chain.filter(exchange);
            } else if (this.authService.isBAuthIgnoredUrl(currentUri)) {
                return chain.filter(exchange);
            } else if (config.getIgnoreAuthUrlStream().anyMatch((ignoreUrl) -> {
                return PATH_MATCHER.match(ignoreUrl, currentUri);
            })) {
                return chain.filter(exchange);
            } else {
                try {
                    String ssoToken = this.authService.getAccessTokenFromRequest(request);
                    Response invalidResponse = null;
                    if (StringUtils.isNotBlank(ssoToken)) {
                        Response tokenResponse = this.authService.verifyToken(ssoToken, (String) null);
                        if (tokenResponse.isSuccess()) {
                            return chain.filter(exchange);
                        }

                        invalidResponse = (new Response()).failure(CompositeAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_UNAUTHORIZED.message).code(CompositeAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_UNAUTHORIZED.code);
                    } else {
                        Integer userId = this.authService.getUserIdFromRequest(request);
                        if (userId != null && userId > 0) {
                            return chain.filter(exchange);
                        }

                        if (userId != null && userId == -1) {
                            invalidResponse = (new Response()).failure(CompositeAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_INVALID.message).code(CompositeAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_INVALID.code);
                        } else {
                            invalidResponse = (new Response()).failure(CompositeAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_UNAUTHORIZED.message).code(CompositeAuthGatewayFilterFactory.UserAuthExceptionEnum.USER_UNAUTHORIZED.code);
                        }
                    }

                    ServerHttpResponse httpResponse = exchange.getResponse();
                    httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                    DataBuffer buffer = ResponseUtils.getResponeBuffer(httpResponse, invalidResponse);
                    return httpResponse.writeWith(Mono.just(buffer));
                } catch (Exception var10) {
                    log.error("gateway error on cauth : {}", var10.getMessage());
                    var10.printStackTrace();
                    return chain.filter(exchange);
                }
            }
        };
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
            } else if (!(o instanceof CompositeAuthGatewayFilterFactory.Config)) {
                return false;
            } else {
                CompositeAuthGatewayFilterFactory.Config other = (CompositeAuthGatewayFilterFactory.Config) o;
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
            return other instanceof CompositeAuthGatewayFilterFactory.Config;
        }

        public int hashCode() {
            Object routeIgnoreAuthUrls = this.getRouteIgnoreAuthUrls();
            return 59 + (routeIgnoreAuthUrls == null ? 43 : routeIgnoreAuthUrls.hashCode());
        }

        public String toString() {
            return "CompositeAuthGatewayFilterFactory.Config(routeIgnoreAuthUrls=" + this.getRouteIgnoreAuthUrls() + ")";
        }
    }
}
