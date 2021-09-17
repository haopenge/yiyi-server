package com.peppa.common.handler.pool;

import java.io.Serializable;
import java.util.Map;


public class HttpClientConfiguration
        implements Serializable {
    public static final int DEFAULT_MAX_CONNECTIONS = 1024;
    public static final long DEFAULT_CONNECTION_TIMEOUT = 500L;
    public static final long DEFAULT_SOCKET_TIMEOUT = 1000L;
    public static long DEFAULT_KEEP_ALIVE_TIME = 6000L;


    public static final int DEFAULT_HTTP_RETRY_TIMES = 1;

    public static final boolean DEFAULT_HTTP_RETRY_ON_FAILURE = true;

    public static final long DEFAULT_REQUEST_TIMEOUT = 3000L;

    public static int MAX_IDLE_CONNECTIONS = 3;


    private long connectionTimeout = 500L;

    private long socketTimeout = 1000L;


    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;


    private int retryTimes = 1;


    private boolean retryOnFailure = true;

    private Map<String, String> globalHeaders;

    private long requestTimeout = 3000L;

    private boolean dnsResolverEnabled = false;

    private NamingResolver namingResolver;

    public boolean isDnsResolverEnabled() {
        return this.dnsResolverEnabled;
    }

    public HttpClientConfiguration setDnsResolverEnabled(boolean dnsResolverEnabled) {
        this.dnsResolverEnabled = dnsResolverEnabled;
        return this;
    }

    public NamingResolver getNamingResolver() {
        return this.namingResolver;
    }

    public HttpClientConfiguration setNamingResolver(NamingResolver namingResolver) {
        this.namingResolver = namingResolver;
        return this;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout.longValue();
    }

    public void setSocketTimeout(Long socketTimeout) {
        this.socketTimeout = socketTimeout.longValue();
    }

    public void setKeepAliveTime(Long keepAliveTime) {
        this.keepAliveTime = keepAliveTime.longValue();
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes.intValue();
    }

    public void setRetryOnFailure(Boolean retryOnFailure) {
        this.retryOnFailure = retryOnFailure.booleanValue();
    }

    public Long getConnectionTimeout() {
        return Long.valueOf(this.connectionTimeout);
    }

    public Long getSocketTimeout() {
        return Long.valueOf(this.socketTimeout);
    }

    public Long getKeepAliveTime() {
        return Long.valueOf(this.keepAliveTime);
    }

    public Integer getRetryTimes() {
        return Integer.valueOf(this.retryTimes);
    }

    public Boolean getRetryOnFailure() {
        return Boolean.valueOf(this.retryOnFailure);
    }

    public Map<String, String> getGlobalHeaders() {
        return this.globalHeaders;
    }

    public void setGlobalHeaders(Map<String, String> globalHeaders) {
        this.globalHeaders = globalHeaders;
    }

    public int getMaxConnections() {
        return 1024;
    }

    public Long getRequestTimeout() {
        return Long.valueOf(this.requestTimeout);
    }


    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }


    public static HttpClientConfiguration common(Map<String, String> globalHeaders) {
        HttpClientConfiguration configuration = new HttpClientConfiguration();
        configuration.setConnectionTimeout(Long.valueOf(500L));
        configuration.setSocketTimeout(Long.valueOf(1000L));
        configuration.setKeepAliveTime(Long.valueOf(DEFAULT_KEEP_ALIVE_TIME));
        configuration.setRetryOnFailure(Boolean.valueOf(true));
        configuration.setRetryTimes(Integer.valueOf(1));
        if (globalHeaders != null) {
            configuration.setGlobalHeaders(globalHeaders);
        }
        configuration.setRequestTimeout(3000L);
        return configuration;
    }

    public static HttpClientConfiguration common() {
        return common(null);
    }
}


