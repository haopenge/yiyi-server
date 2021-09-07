package com.peppa.common.grayconfig.Strategy;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

public interface Strategy extends Comparable<Strategy> {
    public static final String A_IP = "A_IP_";

    public static final String A_DEV = "A_DEV_";

    public static final String S_IP = "S_REGIP_";

    public static final String S_NA = "S_REGNA_";

    public static final String S_TAG = "S_TAG_";

    public static final String H_PODENV = "H_PODENV_";

    public static final String H_SVIP = "H_SVIP_";

    public static final String H_IPS = "H_IPS_";

    public static final String TAG_WHITELIST = "graywl";

    public static final String TAG_BLACKLIST = "graybl";

    public static final String DEFAULT_STRATEGY = "DEF";

    public static final int SORT_H_IPS = 1;

    public static final int SORT_H_SVIP = 2;

    public static final int SORT_S_TAG = 3;

    public static final int SORT_S_IP = 4;

    public static final int SORT_S_NA = 5;

    public static final int SORT_A_IP = 6;

    public static final int SORT_H_PODENV = 7;

    public static final int SORT_A_DEV = 8;

    public static final String DELIM_ITEM = ",";

    public static final String DELIM_VALUE = ":";

    String getName();

    Server getServer(ILoadBalancer paramILoadBalancer);

    int getOrder();
}