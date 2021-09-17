/*     */
package com.peppa.common.feign;

import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(prefix = "peppa", name = {"feigncheckpoint"}, havingValue = "true")
public class FeignListConfig {
    private static final Logger log = LoggerFactory.getLogger(FeignListConfig.class);


    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    private static UrlCache urlCache = new UrlCache();


    @PostConstruct
    public synchronized void init() {
        String[] feignnames = this.applicationContext.getBeanNamesForAnnotation(FeignClient.class);


        for (String beanname : feignnames) {

            try {
                Class<?> c = Class.forName(beanname);

                Annotation feignanno = (Annotation) c.getAnnotation(FeignClient.class);
                Annotation feignrequestmapping = (Annotation) c.getAnnotation(RequestMapping.class);

                String reqmapping = getRequestMappingUrl((RequestMapping) feignrequestmapping);

                String serverName = getServerName(feignanno);

                String preUrl = fomatUrl(((FeignClient) feignanno).path());

                Method[] m = c.getDeclaredMethods();
                for (Method method : m) {
                    Annotation[] annotations = method.getDeclaredAnnotations();
                    for (Annotation anno : annotations) {


                        try {


                            String url = null;

                            if (anno instanceof RequestMapping) {

                                url = ((((RequestMapping) anno).value()).length > 0) ? getAnnoValue(((RequestMapping) anno).value()) : getAnnoValue(((RequestMapping) anno).path());
                            } else if (anno instanceof GetMapping) {
                                url = ((((GetMapping) anno).value()).length > 0) ? getAnnoValue(((GetMapping) anno).value()) : getAnnoValue(((GetMapping) anno).path());
                            } else if (anno instanceof PostMapping) {
                                url = ((((PostMapping) anno).value()).length > 0) ? getAnnoValue(((PostMapping) anno).value()) : getAnnoValue(((PostMapping) anno).path());
                            } else if (anno instanceof DeleteMapping) {
                                url = ((((DeleteMapping) anno).value()).length > 0) ? getAnnoValue(((DeleteMapping) anno).value()) : getAnnoValue(((DeleteMapping) anno).path());
                            } else if (anno instanceof PutMapping) {
                                url = ((((PutMapping) anno).value()).length > 0) ? getAnnoValue(((PutMapping) anno).value()) : getAnnoValue(((PutMapping) anno).path());
                            } else if (anno instanceof PatchMapping) {
                                url = ((((PatchMapping) anno).value()).length > 0) ? getAnnoValue(((PatchMapping) anno).value()) : getAnnoValue(((PatchMapping) anno).path());
                            }
                            url = fomatUrlReal(url);
                            addToChace(url, preUrl, reqmapping, serverName);
                        } catch (Throwable e) {
                            log.warn("feign annotation 解析失败,类：{},方法：{},{}", new Object[]{beanname, method.toString(), e.getCause()});
                        }

                    }
                }
            } catch (Throwable e) {
                log.warn("feign annotation 解析失败,类：{},{}", beanname, e.toString());
            }
        }


        urlCache.printAll();
    }

    private String getAnnoValue(String[] strings) {
        if (strings == null || strings.length == 0) {
            return "";
        }
        return strings[0];
    }

    private void addToChace(String url, String preUrl, String reqmapping, String serverName) {
        if (url != null) {
            if (url.equals("")) {
                addToChace("/", preUrl, reqmapping, serverName);
            }
            url = preUrl.concat(reqmapping).concat(url);


            if (url.indexOf("{") >= 0) {
                urlCache.addRestfulUrl(url, serverName);
            } else {

                urlCache.addUrl(url, serverName);
            }
        }
    }

    private String getRequestMappingUrl(RequestMapping anno) {
        if (anno == null) {
            return "";
        }
        if (anno.value() != null && (anno.value()).length > 0 &&
                anno.value()[0].length() > 0) {
            return fomatUrlReal(anno.value()[0]);
        }

        if (anno.path() != null && (anno.path()).length > 0 &&
                anno.path()[0].length() > 0) {
            return fomatUrlReal(anno.path()[0]);
        }

        return "";
    }


    private String fomatUrl(String url) {
        url = fomatUrlReal(url);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private String fomatUrlReal(String url) {
        if (url == null || url.length() == 0) {
            return "";
        }
        url = this.environment.resolvePlaceholders(url);
        if (url.equals("/")) {
            return url;
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return url;
    }


    public static UrlCache.ServerUrl getUrl(String servername, String url) {
        try {
            UrlCache.ServerUrl returl = urlCache.getFromUrlMap(url);
            if (returl != null) {
                return returl;
            }
            returl = urlCache.getFromRestfulUrlList(servername, url);
            if (returl != null) {
                return returl;
            }
        } catch (Exception e) {
            log.error("getUrl 错误", e);
        }

        return null;
    }

    private String getServerName(Annotation annotation) {
        String serverName = ((FeignClient) annotation).name();
        String serverUrl = ((FeignClient) annotation).url();
        if (serverUrl != null && serverUrl.length() > 0) {
            serverName = this.environment.resolvePlaceholders(serverUrl);
        }
        if (serverName == null || serverName.length() == 0) {
            serverName = ((FeignClient) annotation).value();
        }

        serverName = this.environment.resolvePlaceholders(serverName);
        if (serverName.endsWith("/")) {
            serverName = serverName.substring(0, serverName.length() - 1);
        }
        if (serverName.startsWith("http")) {
            try {
                URL u = new URL(serverName);
                serverName = u.getHost();
            } catch (Exception e) {
                log.error("servername 解析失败：{}", serverName, e);
            }
        }

        return serverName;
    }


    static class UrlCache {
        private ConcurrentHashMap<String, ServerUrl> URLMAP = new ConcurrentHashMap<>();
        private Set<ServerUrl> RESTFUL_URLLIST = (Set<ServerUrl>) new ConcurrentSet();

        private ConcurrentHashMap<String, ServerUrl> URLMAP_CACHE = new ConcurrentHashMap<>();
        private Set<ServerUrl> RESTFUL_URLLIST_CACHE = (Set<ServerUrl>) new ConcurrentSet();


        void addUrl(String url, String servername) {
            ServerUrl serverUrl = this.URLMAP.get(url);
            if (serverUrl == null) {
                this.URLMAP.put(url, new ServerUrl(url, servername));
            } else {
                serverUrl.setServer(servername);
            }
        }

        void addRestfulUrl(String url, String servername) {
            for (ServerUrl serverurl : this.RESTFUL_URLLIST) {
                if (serverurl.getUrl().equals("url")) {
                    serverurl.setServer(servername);
                    return;
                }
            }
            this.RESTFUL_URLLIST.add(new ServerUrl(url, servername));
        }

        ServerUrl getFromUrlMap(String url) {
            ServerUrl serverUrl = this.URLMAP_CACHE.get(url);
            if (serverUrl != null) {
                return serverUrl;
            }
            serverUrl = this.URLMAP.get(url);
            if (serverUrl != null) {
                this.URLMAP_CACHE.put(url, serverUrl);
                return serverUrl;
            }
            return null;
        }

        ServerUrl getFromRestfulUrlList(String servername, String url) {
            AntPathMatcher matcher = new AntPathMatcher(File.separator);
            for (ServerUrl serverUrl : this.RESTFUL_URLLIST_CACHE) {
                if (matcher.match(serverUrl.getUrl(), url) &&
                        serverUrl.containServer(servername)) {
                    return serverUrl;
                }
            }
            for (ServerUrl serverUrl : this.RESTFUL_URLLIST) {

                if (matcher.match(serverUrl.getUrl(), url) &&
                        serverUrl.containServer(servername)) {
                    this.RESTFUL_URLLIST_CACHE.add(serverUrl);
                    return serverUrl;
                }
            }

            return null;
        }

        void printAll() {
            String resturls = "";
            for (ServerUrl key : this.RESTFUL_URLLIST) {
                resturls = resturls + key.getServerStrings() + ":" + key.getUrl() + ",";
            }
            FeignListConfig.log.info("restful url初始化完成:\n{}", resturls);
            resturls = "";
            for (ServerUrl key : this.URLMAP.values()) {
                resturls = resturls + key.getServerStrings() + ":" + key.getUrl() + ",";
            }
            FeignListConfig.log.info("normal url初始化完成:\n{}", resturls);
        }

        public static class ServerUrl {
            private String url;
            private Set<String> servers = (Set<String>) new ConcurrentSet();
            private String firstServer;

            ServerUrl(String url, String servername) {
                this.url = url;
                this.servers.add(servername);
                this.firstServer = servername;
            }

            String getUrl() {
                return this.url;
            }

            boolean containServer(String servername) {
                return this.servers.contains(servername);
            }

            String getServerStrings() {
                if (this.servers.size() == 1) {
                    return getFirstServer();
                }
                String sv = "";
                for (String servername : this.servers) {
                    sv = sv + servername + "|";
                }
                return sv;
            }

            String getFirstServer() {
                return this.firstServer;
            }

            void setServer(String servername) {
                this.servers.add(servername);
            }
        }
    }

    public static class ServerUrl {
        void setServer(String servername) {
            this.servers.add(servername);
        }


        private String url;
        private Set<String> servers = (Set<String>) new ConcurrentSet();
        private String firstServer;

        ServerUrl(String url, String servername) {
            this.url = url;
            this.servers.add(servername);
            this.firstServer = servername;
        }

        String getUrl() {
            return this.url;
        }

        boolean containServer(String servername) {
            return this.servers.contains(servername);
        }

        String getServerStrings() {
            if (this.servers.size() == 1)
                return getFirstServer();
            String sv = "";
            for (String servername : this.servers)
                sv = sv + servername + "|";
            return sv;
        }

        String getFirstServer() {
            return this.firstServer;
        }
    }

}


