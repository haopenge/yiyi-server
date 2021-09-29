//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.peppa.service.gateway.filter;

import com.peppa.common.response.Response;
import com.peppa.service.gateway.service.AuthService;
import com.peppa.service.gateway.utils.ReactiveRequestUtils;
import com.peppa.service.gateway.utils.ResponseUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
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
public class MicroAppAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<MicroAppAuthGatewayFilterFactory.Config> {
    private static final Logger log = LoggerFactory.getLogger(MicroAppAuthGatewayFilterFactory.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    public static final String ROUTE_IGNORE_AUTH_URL_KEY = "routeIgnoreAuthUrls";
    public static final String IGNORE_URL_SEPERATOR = "\\|";
    private static final String CLIEND_ID_IN_HEADER_KEY = "sso-client-id";
    @Resource
    private AuthService authService;

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("routeIgnoreAuthUrls");
    }

    public MicroAppAuthGatewayFilterFactory() {
        super(MicroAppAuthGatewayFilterFactory.Config.class);
    }

    public GatewayFilter apply(MicroAppAuthGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String currentUri = request.getPath().toString();
            log.debug("current uri is : {}", currentUri);
            if (this.authService.isBAuthIgnoredUrl(currentUri)) {
                return chain.filter(exchange);
            } else if (config.getIgnoreAuthUrlStream().anyMatch((ignoreUrl) -> {
                return PATH_MATCHER.match(ignoreUrl, currentUri);
            })) {
                return chain.filter(exchange);
            } else {
                String token = this.authService.getAccessTokenFromRequest(request);
                String clientId = ReactiveRequestUtils.getHeader(request, "sso-client-id");

                try {
                    Response response = this.authService.verifyToken(clientId, token, currentUri);
                    if (!response.isSuccess()) {
                        ServerHttpResponse httpResponse = exchange.getResponse();
                        httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        DataBuffer buffer = ResponseUtils.getResponeBuffer(httpResponse, response);
                        return httpResponse.writeWith(Mono.just(buffer));
                    }
                } catch (Exception var11) {
                    log.error("gateway error on verify auth : {}", var11.getMessage());
                    var11.printStackTrace();
                }

                return chain.filter(exchange);
            }
        };
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
            } else if (!(o instanceof MicroAppAuthGatewayFilterFactory.Config)) {
                return false;
            } else {
                MicroAppAuthGatewayFilterFactory.Config other = (MicroAppAuthGatewayFilterFactory.Config)o;
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
            return other instanceof MicroAppAuthGatewayFilterFactory.Config;
        }

        public int hashCode() {
            String routeIgnoreAuthUrls = this.getRouteIgnoreAuthUrls();
            return 59 + (routeIgnoreAuthUrls == null ? 43 : routeIgnoreAuthUrls.hashCode());
        }

        public String toString() {
            return "MicroAppAuthGatewayFilterFactory.Config(routeIgnoreAuthUrls=" + this.getRouteIgnoreAuthUrls() + ")";
        }
    }
}
