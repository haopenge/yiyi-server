package com.peppa.service.gateway.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class ReactiveRequestUtils {
    private static final Logger log = LoggerFactory.getLogger(ReactiveRequestUtils.class);

    public static String getRealIp(ServerHttpRequest r) {
        HttpHeaders headers = r.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = r.getRemoteAddress().toString();
        }
        if ("127.0.0.1".equals(ip) || ip.contains("0:0:0:0:0:0:0:1")) {
            ip = getMyIp();
        }

        if (ip != null && ip.length() > 15 &&
                ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }

        return ip;
    }

    private static String getMyIp() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                log.debug(netInterface.getName());
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof java.net.Inet4Address) {
                        log.debug("本机的IP = " + ip.getHostAddress());
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("getMyIp()", e);
        }
        return "";
    }

    public static String getReferer(ServerHttpRequest r) {
        String referer = r.getHeaders().getFirst("referer");
        if (StringUtils.isBlank(referer)) {
            return "";
        }
        return referer;
    }

    public static String getHeader(ServerHttpRequest r, String key) {
        HttpHeaders headers = r.getHeaders();
        return headers.getFirst(key);
    }
}
