package com.peppa.common.feign;


import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.URL;


@Configuration
@ConditionalOnProperty(prefix = "peppa", name = {"feigncheckpoint"}, havingValue = "true")
public class PrometheusFeignCounter {
    private static final Logger log = LoggerFactory.getLogger(PrometheusFeignCounter.class);


    private static boolean ischeck = false;


    @Autowired
    private CollectorRegistry collectorRegistry;


    private static Counter feignCounter = null;

    @Value("${spring.application.name:}")
    private String appname;

    @Value("${peppa.feigncheckpoint.log404:true}")
    private String log404;

    private static String Log404 = "true";
    private static String APPNAME;

    @PostConstruct
    void init() {
        ischeck = true;
        APPNAME = this.appname;


        feignCounter = (Counter) ((Counter.Builder) ((Counter.Builder) ((Counter.Builder) Counter.build().name("feign_call_counter")).labelNames(new String[]{"application", "server", "method", "url", "retcode", "errorip"})).help("feign call response counter")).register(this.collectorRegistry);
        Log404 = this.log404;
    }

    @ApolloConfigChangeListener(interestedKeys = {"peppa.feigncheckpoint.log404"})
    private void configChangeListter(ConfigChangeEvent changeEvent) {
        if (changeEvent.isChanged("peppa.feigncheckpoint.log404")) {
            this.log404 = changeEvent.getChange("peppa.feigncheckpoint.log404").getNewValue();
            Log404 = this.log404;
            log.info("new Log404:{}", Log404);
        }
    }

    public static void count(String method, String url, String code) {
        String servername;
        if (!ischeck) {
            return;
        }

        String errorip = "";
        try {
            URL u = new URL(url);
            url = u.getPath();
            errorip = servername = u.getHost();
        } catch (Exception e) {
            servername = "NONE";
            if (!"true".equals(Log404)) {
                log.warn("promethus埋点收录的请求异常（联系架构组）：{}", url);
            }
        }

        String formalurl = url;
        if (!servername.equals("NONE")) {
            FeignListConfig.UrlCache.ServerUrl serverUrl = FeignListConfig.getUrl(servername, url);
            if (serverUrl == null) {
                formalurl = "404-" + url;
                if (!"true".equals(Log404)) {
                    log.warn("promethus埋点未收录的请求（联系架构组）：{}", formalurl);
                }
            } else {

                if (!serverUrl.containServer(servername)) {
                    servername = serverUrl.getServerStrings();
                    if (!"true".equals(Log404)) {
                        log.warn("promethus埋点未收录的请求（联系架构组）：servername：{},{}", servername, formalurl);
                    }
                }
                formalurl = serverUrl.getUrl();
                if (code.equals("200")) {
                    errorip = "";
                }
                ((Counter.Child) feignCounter.labels(new String[]{APPNAME, servername, method, formalurl, code, errorip})).inc();
            }
        }
    }
}
