package com.peppa.common.handler.sentinel;


import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.adapter.servlet.util.FilterUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@ConditionalOnProperty(name = {"spring.cloud.sentinel.filter.enabled"}, havingValue = "false")
public class PeppaSentinelRestfulFilter implements Filter {

    private boolean httpMethodSpecify = false;
    private List<String> excludeUrls = new ArrayList<>();
    private final Map<HandlerMethod, String> handlerMethodUrlMap = new ConcurrentHashMap<>(32);

    @Autowired
    private DispatcherServlet dispatcherServlet;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();


    @Value("${peppa.sentinel.restful.enabled:true}")
    private Boolean restfulenable;


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!this.restfulenable) {
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String originalTarget = FilterUtil.filterTarget(httpServletRequest);
        if (this.excludeUrls.stream().anyMatch(url -> this.antPathMatcher.match(url, originalTarget))) {
            chain.doFilter(request, response);
            return;
        }
        Entry entry = null;
        Entry methodEntry = null;

        try {
            String target = resolveTarget(httpServletRequest);
            if (!StringUtil.isEmpty(target)) {

                String origin = parseOrigin(httpServletRequest);

                ContextUtil.enter("sentinel_web_servlet_context", origin);


                if (this.httpMethodSpecify) {
                    methodEntry = SphU.entry(httpServletRequest.getMethod().toUpperCase() + ":" + target, EntryType.IN);
                } else {
                    entry = SphU.entry(target, EntryType.IN);
                }
            }
            chain.doFilter(request, response);
        } catch (BlockException e) {

            WebCallbackManager.getUrlBlockHandler().blocked(httpServletRequest, httpServletResponse, e);
        } catch (IOException | RuntimeException e2) {
            Tracer.trace(e2);
            throw e2;
        } catch (ServletException e3) {
            Tracer.trace((Throwable) e3);
            throw e3;
        } finally {
            if (methodEntry != null) {
                methodEntry.exit();
            }
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }


    protected String resolveTarget(HttpServletRequest request) {
        String target = FilterUtil.filterTarget(request);
        String method = request.getMethod();
        String pattern = "";
        assert dispatcherServlet.getHandlerMappings() != null;
        for (HandlerMapping mapping : dispatcherServlet.getHandlerMappings()) {
            HandlerExecutionChain handler = null;
            try {
                handler = mapping.getHandler(request);

                if (handler != null) {
                    Object handlerObject = handler.getHandler();
                    if (handlerObject instanceof HandlerMethod) {
                        HandlerMethod handlerMethod = (HandlerMethod) handlerObject;

                        pattern = this.handlerMethodUrlMap.getOrDefault(handlerMethod, "");
                        if (StringUtils.isEmpty(pattern)) {

                            pattern = resolveResourceNameHandlerMethod(handlerMethod);
                            this.handlerMethodUrlMap.put(handlerMethod, pattern);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        UrlCleaner urlCleaner = WebCallbackManager.getUrlCleaner();
        if (!StringUtils.isEmpty(pattern)) {
            target = pattern;
        }
        if (urlCleaner != null) {


            target = urlCleaner.clean(target);
        }

        if (!"GET".equals(method) && method != null) {
            target = target.concat("_").concat(method);
        }
        return target;
    }


    private String resolveResourceNameHandlerMethod(HandlerMethod handlerMethod) {
        String typeMapping = "";

        RequestMapping typeRequestMapping = (RequestMapping) AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequestMapping.class);
        if (typeRequestMapping != null && (typeRequestMapping.value()).length > 0) {
            typeMapping = typeRequestMapping.value()[0];
        }

        RequestMapping methodRequestMapping = (RequestMapping) AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequestMapping.class);

        if (methodRequestMapping == null || (methodRequestMapping.value()).length == 0) {
            return "";
        }
        String methodMapping = methodRequestMapping.value()[0];
        if (typeMapping.length() > 1 && typeMapping.endsWith("/")) {
            typeMapping = typeMapping.substring(0, typeMapping.length() - 1);
        }
        return typeMapping + methodMapping;
    }


    public void init(FilterConfig filterConfig) {
        this.httpMethodSpecify = Boolean.parseBoolean(filterConfig.getInitParameter("HTTP_METHOD_SPECIFY"));
        String excludeUrlsString = filterConfig.getInitParameter("EXCLUDE_URLS");
        if (!StringUtils.isEmpty(excludeUrlsString)) {
            this.excludeUrls = Arrays.asList(excludeUrlsString.split(","));
        }
    }


    private String parseOrigin(HttpServletRequest request) {
        RequestOriginParser originParser = WebCallbackManager.getRequestOriginParser();
        String origin = "";
        if (originParser != null) {
            origin = originParser.parseOrigin(request);
            if (StringUtil.isEmpty(origin)) {
                return "";
            }
        }
        return origin;
    }

    public void destroy() {
    }
}


