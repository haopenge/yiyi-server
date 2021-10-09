package com.yiyi.common.handler;

import com.ctrip.framework.apollo.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class LogRequestInterceptor extends HandlerInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(LogRequestInterceptor.class);

    ThreadLocal<Long> startTime = new ThreadLocal<>();

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            if (!isOpen()) {
                return true;
            }
            this.startTime.set(System.currentTimeMillis());
            String uri = request.getRequestURI();
            String ip = getRequestIpAddr(request);
            String param = toJson(request.getParameterMap());
            String method = request.getMethod();
            String sv = getRequestServerName(request);
            MDC.put("uri", uri);
            MDC.put("reqsvr", sv);
            MDC.put("ip", ip);
            MDC.put("method", method);
            MDC.put("param", param);
            String podenv = request.getHeader("yiyi-podenv");
            if (podenv != null) {
                MDC.put("podenv", podenv);
                log.info("Request access info:uri=[{}], reqsvr=[{}], ip=[{}], method=[{}], param={}, podenv={}", uri, sv, ip, method, param, podenv);
            } else {
                log.info("Request access info:uri=[{}], reqsvr=[{}], ip=[{}], method=[{}], param={}, podenv=no", uri, sv, ip, method, param);
            }

        } catch (Exception e) {
            log.error("common.log", e);
        }

        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            if (!isOpen()) {
                return;
            }
            String uri = request.getRequestURI();
            String ip = getRequestIpAddr(request);
            String param = toJson(request.getParameterMap());
            String method = request.getMethod();
            Long start = this.startTime.get();
            long cost = (start == null) ? 0L : (System.currentTimeMillis() - start);
            int responseCode = response.getStatus();
            MDC.put("code", "" + responseCode);
            MDC.put("cost", "" + cost);
            log.info("Request response info:uri=[{}], ip=[{}], method=[{}], param={}, cost=[{}]ms, response status=[{}]", uri, ip, method, param, cost, Integer.valueOf(responseCode));
            MDC.clear();
        } catch (Exception e) {
            log.error("common.log", e);
        }
    }

    private boolean isOpen() {
        if (ConfigService.getConfig("t1.springcloud").getIntProperty("common.log.isopen", 0) == 1)
            return true;
        return false;
    }

    private String toJson(Map<String, String[]> map) {
        String str = "";
        Iterator<Map.Entry<String, String[]>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> entry = iterator.next();
            str = str.concat(entry.getKey());
            str = str.concat(":");
            str = str.concat(Arrays.toString((Object[]) entry.getValue()));
            str = str.concat("|");
        }
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static String getRequestServerName(HttpServletRequest request) {
        try {
            String callip = request.getHeader("yiyi-reqsvr");
            if (callip != null && callip.trim().length() > 0) {
                return callip.trim();
            }
            return "";
        } catch (Exception e) {
            log.error("getRequestServerName error:", e);
            return "error";
        }
    }

    public static String getRequestIpAddr(HttpServletRequest request) {
        try {
            String callip = request.getHeader("yiyi-call-ip");
            if (callip != null && callip.trim().length() > 0) {
                return callip.trim();
            }
            String ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            if (ipAddress.equals("0:0:0:0:0:0:0:1")) {


                ipAddress = "127.0.0.1";
            }

            if (ipAddress.length() > 15 && ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }

            return ipAddress;
        } catch (Exception e) {
            log.error("getRequestIpAddr error:", e);
            return "0.0.0.0";
        }
    }
}

