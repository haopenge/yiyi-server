package com.yiyi.filter;


import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    private Logger logger = LoggerFactory.getLogger(AuthGatewayFilterFactory.class);

    /**
     * 用户登录状态token
     */
    private static final String USER_TOKEN = "user_token";

    public AuthGatewayFilterFactory(){
        super(Config.class);
        logger.info("AuthGatewayFilterFactory init");
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("ignoreUrlListStr");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            // 校验是否是 不用登录的URL
            String path = request.getPath().toString();
            logger.info("AuthGatewayFilterFactory.apply path:{}",path);


            String ignoreUrlListStr = config.ignoreUrlListStr;
            logger.info("AuthGatewayFilterFactory.apply ignoreUrlListStr={}",ignoreUrlListStr);

            boolean ignoreOk = Arrays.asList(ignoreUrlListStr.split("\\|")).contains(path);
            if(ignoreOk){
                return chain.filter(exchange);
            }

            // 校验是否登录
            HttpHeaders headers = request.getHeaders();
            String userToken = headers.getFirst(USER_TOKEN);
            if(StringUtils.isEmpty(userToken)){
                // 返回未登录提示
                ServerHttpResponse response = exchange.getResponse();
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);

                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("code",-1000003);
                bodyMap.put("message","未登录");

                byte[] responseByteArray = JSON.toJSONBytes(bodyMap);
                DataBuffer responseBuffer = response.bufferFactory().allocateBuffer(responseByteArray.length).write(responseByteArray);
                return response.writeWith(Mono.just(responseBuffer));
            }
            logger.info("AuthGatewayFilterFactory.apply  user-token={}",userToken);
            return chain.filter(exchange);
        };
    }

    public static class Config {
        private String ignoreUrlListStr;

        public String getIgnoreUrlListStr() {
            return ignoreUrlListStr;
        }

        public void setIgnoreUrlListStr(String ignoreUrlListStr) {
            this.ignoreUrlListStr = ignoreUrlListStr;
        }
    }
}
