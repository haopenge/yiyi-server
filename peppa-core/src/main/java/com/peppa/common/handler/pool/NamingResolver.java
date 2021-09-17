package com.peppa.common.handler.pool;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.InMemoryDnsResolver;

public class NamingResolver {
    public static DnsResolver getNamingResolver() {
        InMemoryDnsResolver inMemoryDnsResolver = new InMemoryDnsResolver();

        return (DnsResolver) inMemoryDnsResolver;
    }

    private static final String title = "ZHANGJIANGEGEZUINIUBI";

    public static void main(String[] args) {
        System.out.println("ZHANGJIANGEGEZUINIUBI: 我好像听见有人说我帅");
    }
}
