package com.husky.config;

import com.husky.util.IpUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Objects;

@Configuration
public class FeignHeaderIntercept implements RequestInterceptor {

    private Logger logger = LoggerFactory.getLogger(FeignHeaderIntercept.class);

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if(Objects.isNull(requestAttributes)){
            return;
        }

        HttpServletRequest request = requestAttributes.getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();

        if(Objects.isNull(headerNames)){
            return ;
        }

        boolean xForwardIpExist = false;
        while (headerNames.hasMoreElements()){
            String name = headerNames.nextElement();
            String values = request.getHeader(name);

            // 获取用户真实IP
            if (name.equalsIgnoreCase(HttpHeaderConstant.X_FORWARDED_FOR)) {
                if(IpUtils.isLocalAddress(values)){
                    values = IpUtils.getIpAddr(request);
                }
                template.header(HttpHeaderConstant.X_FORWARDED_FOR,values);

                xForwardIpExist = true;
            }

            // 自定义http header 获取
            if(name.startsWith(HttpHeaderConstant.ENV_PREFIX)){
                values = request.getHeader(name);
                logger.info("http 拦截 header : name = {},values = {}",name,values);
                template.header(name,values);
            }
        }

        if(!xForwardIpExist){
            String remoteAddr = IpUtils.getIpAddr(request);
            logger.info("http x-forwarded-for 增加remoteAddr header : {}",remoteAddr);
            template.header(HttpHeaderConstant.X_FORWARDED_FOR,remoteAddr);
        }
    }
}
