package com.peppa.common.grayconfig;

import com.peppa.common.util.IpUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


@Configuration
@ConditionalOnProperty(prefix = "peppa", name = {"gray"}, havingValue = "true")
@Slf4j
public class FeignHeaderRequestInterceptor implements RequestInterceptor {

    @Value("${forceHeader:}")
    private String forceHeader;

    private HashMap<String,Object> forceHeaderMap;

    @PostConstruct
    private void initForceHeader() {
        if (this.forceHeader == null || this.forceHeader.trim().equals("")) {
            this.forceHeaderMap = null;
            return;
        }
        this.forceHeaderMap = new HashMap<>();
        try {
            StringTokenizer stringTokenizer = new StringTokenizer(this.forceHeader, ",");
            while (stringTokenizer.hasMoreTokens()) {
                String str = stringTokenizer.nextToken();
                int ind = str.indexOf(":");
                if (ind <= 0 || str.length() == ind + 1)
                    continue;
                String headerkey = str.substring(0, ind).trim();
                String headercont = str.substring(ind + 1).trim();
                forceHeaderMap.put(headerkey, headercont);
            }
        } catch (Exception e) {
            log.error("initForceHeader", e);
            this.forceHeaderMap = null;
        }
    }


    private void setForceHeader(RequestTemplate template) {
        String podkey = "huohua-".concat("podenv");
        String podenv = ThreadAttributes.getHeaderValue("podenv");
        if (podenv != null && podenv.trim().length() > 0) {
            log.info("feign调用:{}threadlocal中带独立环境标记{},继续在feign调用中传递下去", template.url(), podenv);
            template.header(podkey, podenv);
        }

        String myip = IpUtils.getMyIp();
        if (myip != null) {
            template.header("huohua-".concat("call-ip"), myip);
        }
        if (this.forceHeaderMap == null)
            return;
        try {
            Set<Map.Entry<String, Object>> entries = forceHeaderMap.entrySet();

            entries.forEach(entry -> {
                String key =  entry.getKey();
                String Value = (String) entry.getValue();
                template.header(key, Value);
                ThreadAttributes.setThreadAttribute(key, Value);
            });
        } catch (Exception e) {
            log.error("setForceHeader", e);
        }
    }


    public void apply(RequestTemplate template) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                setForceHeader(template);

                return;
            }
            HttpServletRequest request = attributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            boolean find_xforwardid = false;
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();

                    if (name.equals("x-forwarded-for")) {
                        String values = request.getHeader(name);
                        template.header(name,values);
                        ThreadAttributes.setThreadAttribute("x-forwarded-for", values);
                        find_xforwardid = true;
                    }

                    if (name.startsWith("huohua-")) {
                        String values = request.getHeader(name);
                        template.header(name, values);
                        ThreadAttributes.setThreadAttribute(name, values);
                    }
                }
            }


            if (!find_xforwardid) {
                String remoteAddr = IpUtils.getIpAddr(request);
                template.header("x-forwarded-for", remoteAddr);
                ThreadAttributes.setThreadAttribute("x-forwarded-for", remoteAddr);
            }
            setForceHeader(template);

            log.info("feign interceptor header:{}", template);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}