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
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthFilterConfig> {
    private static final Logger log = LoggerFactory.getLogger(AuthGatewayFilterFactory.class);

    private static final String VERIFY_ACCESS = "access";

    @Resource
    private AuthService authService;


    public AuthGatewayFilterFactory() {
        super(AuthFilterConfig.class);
    }


    public List<String> shortcutFieldOrder() {
        return AuthFilterConfig.SHORTCUT_FIELD_LIST;
    }


    public GatewayFilter apply(AuthFilterConfig config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String currentUri = request.getPath().toString();
            if (this.authService.localVerifyIgnoreUrl(request, config)) {
                return chain.filter(exchange);
            }
            String token = this.authService.getAccessTokenFromRequest(request);
            String clientIdInRequest = ReactiveRequestUtils.getHeader(request, "sso-client-id");
            if (!this.authService.localVerifyToken(exchange, config, clientIdInRequest)) {
                return ResponseUtils.generateInvalidClientBuffer(exchange.getResponse());
            }
            try {
                Response response;
                if (StringUtils.equalsIgnoreCase(config.getVerifyType(), "access")) {
                    response = this.authService.verifyToken(clientIdInRequest, token, currentUri);
                } else {
                    response = this.authService.verifyToken(clientIdInRequest, token, null);
                }
                if (!response.isSuccess()) {
                    return ResponseUtils.generateBuffer(exchange.getResponse(), response);
                }
            } catch (Exception e) {
                log.error("gateway error on verify auth : {}", e.getMessage());
                e.printStackTrace();
            }
            return chain.filter(exchange);
        };
    }
}
