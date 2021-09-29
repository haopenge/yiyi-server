package com.peppa.service.gateway.filter;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AuthFilterConfig {
    public static final String TOKEN_USER_IN_EXCHANGE_KEY = "peppa-token-user";
    public static final String SSO_TOKEN_IN_COOKIE_KEY = "peppa_sso_token";
    public static final String CLIEND_ID_IN_HEADER_KEY = "sso-client-id";

    public void setVerifyType(String verifyType) {
        this.verifyType = verifyType;
    }

    public static final String UID_IN_EXCAHGE_KEY = "_uid_";
    public static final String VERIFY_TYPE_KEY = "verifyType";
    public static final String ALLOWED_CLIENT_ID_KEY = "allowedClientIds";
    public static final String ROUTE_IGNORE_AUTH_URL_KEY = "routeIgnoreAuthUrls";

    public void setAllowedClientIds(String allowedClientIds) {
        this.allowedClientIds = allowedClientIds;
    }

    public void setRouteIgnoreAuthUrls(String routeIgnoreAuthUrls) {
        this.routeIgnoreAuthUrls = routeIgnoreAuthUrls;
    }


    protected boolean canEqual(Object other) {
        return other instanceof AuthFilterConfig;
    }


    public String toString() {
        return "AuthFilterConfig(verifyType=" + getVerifyType() + ", allowedClientIds=" + getAllowedClientIds() + ", routeIgnoreAuthUrls=" + getRouteIgnoreAuthUrls() + ")";
    }


    public static final List<String> SHORTCUT_FIELD_LIST = Arrays.asList("verifyType", "allowedClientIds", "routeIgnoreAuthUrls");
    public static final String IGNORE_URL_SEPERATOR = "\\|";
    private String verifyType;
    private String allowedClientIds;
    private String routeIgnoreAuthUrls;

    public String getVerifyType() {
        return this.verifyType;
    }

    public String getRouteIgnoreAuthUrls() {
        return this.routeIgnoreAuthUrls;
    }

    public Stream<String> getIgnoreAuthUrlStream() {
        if (StringUtils.isBlank(this.routeIgnoreAuthUrls)) {
            return Stream.empty();
        }
        return Stream.of(this.routeIgnoreAuthUrls.split("\\|"));
    }

    public Stream<String> getAllowedClientIds() {
        if (StringUtils.isBlank(this.allowedClientIds)) {
            return Stream.empty();
        }
        return Stream.of(this.allowedClientIds.split("\\|"));
    }
}

