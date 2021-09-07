package com.peppa.common.grayconfig;

import com.peppa.common.grayconfig.apolloconfig.MqGrayApolloConfig;
import com.peppa.common.util.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;


public class RequestHeaderHandlerInterceptor
        implements HandlerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        try {
            this.logger.info("----开始进入请求地址拦截----{}", httpServletRequest.getRequestURL());
            Enumeration<String> eunm = httpServletRequest.getHeaders("x-forwarded-for");
            while (eunm.hasMoreElements()) {
                this.logger.info("----打印header--HTTP_X_FORWARDED_FOR--{}", eunm.nextElement());
            }
        } catch (Exception e) {
            this.logger.error("preHandle", e);
        }
        try {
            MqGrayApolloConfig.tryForcePodEnv();
            HttpServletRequest request = httpServletRequest;
            Enumeration<String> headerNames = request.getHeaderNames();
            boolean find_xforwardid = false;
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    if (name.equals("x-forwarded-for")) {
                        String values = request.getHeader(name);
                        if (IpUtils.isLocalAddress(values).booleanValue()) {
                            values = IpUtils.getIpAddr(request);
                        }
                        ThreadAttributes.setThreadAttribute("x-forwarded-for", values);
                        this.logger.info("http 拦截 header:{},{}", name, values);
                        find_xforwardid = true;
                    }

                    if (name.startsWith("huohua-")) {
                        String values = request.getHeader(name);
                        this.logger.info("http 拦截 header:{},{}", name, values);
                        ThreadAttributes.setThreadAttribute(name, values);
                    }
                }
            }
            if (!find_xforwardid) {
                String remoteAddr = IpUtils.getIpAddr(request);
                this.logger.info("http HTTP_X_FORWARDED_FOR增加remoteAddr header:{}", remoteAddr);
                ThreadAttributes.setThreadAttribute("x-forwarded-for", remoteAddr);
            }

        } catch (Exception e) {
            this.logger.error("preHandle", e);
        }
        return true;
    }


    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadAttributes.remove();
        this.logger.info("---------------------请求处理结束拦截----------------------------");
    }
}