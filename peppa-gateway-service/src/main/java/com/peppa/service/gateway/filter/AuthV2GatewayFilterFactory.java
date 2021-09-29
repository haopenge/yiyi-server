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
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;


@Component
public class AuthV2GatewayFilterFactory extends AbstractGatewayFilterFactory<AuthFilterConfig> {
    private static final Logger log = LoggerFactory.getLogger(AuthV2GatewayFilterFactory.class);

    @Resource
    private AuthService authService;

    public AuthV2GatewayFilterFactory() {
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
            String clientIdInRequest = ReactiveRequestUtils.getHeader(request, "sso-client-id");
            if (!this.authService.localVerifyToken(exchange, config, clientIdInRequest)) {
                return ResponseUtils.generateInvalidClientBuffer(exchange.getResponse());
            }
            String token = this.authService.getAccessTokenFromRequest(request);
            try {
                Response response;
                if (StringUtils.equalsIgnoreCase(config.getVerifyType(), "access")) {
                    response = this.authService.verifyTokenV2(token, currentUri);
                } else {
                    response = this.authService.verifyTokenV2(token, null);
                }
                if (!response.isSuccess()) {
                    ServerHttpResponse httpResponse = exchange.getResponse();
                    httpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    DataBuffer buffer = ResponseUtils.getResponeBuffer(httpResponse, response);
                    return httpResponse.writeWith(Mono.just(buffer));
                }
            } catch (Exception e) {
                log.error("gateway error on verify auth : {}", e.getMessage());
                e.printStackTrace();
            }
            return chain.filter(exchange);
        };
    }
}
