package com.peppa.common.handler.pool;


import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;


public final class OkHttpClientBuilder {
    public static OkHttpClient build(HttpClientConfiguration configuration) {
        return build(configuration, (List<Interceptor>) null);
    }

    public static OkHttpClient build(HttpClientConfiguration configuration, List<Interceptor> interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(configuration.getConnectionTimeout().longValue(), TimeUnit.MILLISECONDS);


        ConnectionPool connectionPool = new ConnectionPool(HttpClientConfiguration.MAX_IDLE_CONNECTIONS, configuration.getKeepAliveTime().longValue(), TimeUnit.MILLISECONDS);
        builder.connectionPool(connectionPool);

        builder.readTimeout(configuration.getSocketTimeout().longValue(), TimeUnit.MILLISECONDS);
        builder.retryOnConnectionFailure(configuration.getRetryOnFailure().booleanValue());
        builder.writeTimeout(configuration.getSocketTimeout().longValue(), TimeUnit.MILLISECONDS);
        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                builder.addInterceptor(interceptor);
            }
        }


        if (configuration.isDnsResolverEnabled()) {
            NamingResolver namingResolver = configuration.getNamingResolver();
        }
        return builder.build();
    }


    public static OkHttpClient build() {
        HttpClientConfiguration configuration = HttpClientConfiguration.common();
        return build(configuration);
    }


    public static OkHttpClient build(int maxIdleConnections) {
        HttpClientConfiguration configuration = HttpClientConfiguration.common();
        HttpClientConfiguration.MAX_IDLE_CONNECTIONS = maxIdleConnections;
        return build(configuration);
    }


    public static OkHttpClient build(int maxIdleConnections, long keepAliveTime) {
        HttpClientConfiguration configuration = HttpClientConfiguration.common();
        HttpClientConfiguration.MAX_IDLE_CONNECTIONS = maxIdleConnections;
        HttpClientConfiguration.DEFAULT_KEEP_ALIVE_TIME = keepAliveTime;
        return build(configuration);
    }
}


