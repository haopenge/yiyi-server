package com.peppa.common.grayconfig;

import java.util.HashMap;
import java.util.Map;


public class ThreadAttributes {

    private static ThreadLocal<Map<String, Object>> threadAttribues = new ThreadLocal<Map<String, Object>>() {
        protected synchronized Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };


    public static Object getThreadAttribute(String name) {
        return ((Map) threadAttribues.get()).get(name);
    }


    public static Object setThreadAttribute(String name, Object value) {
        return ((Map<String, Object>) threadAttribues.get()).put(name, value);
    }

    public static void remove() {
        threadAttribues.remove();
    }


    public static String getHeaderValue(String header_key) {
        String headervalue = (String) getThreadAttribute("huohua-".concat(header_key));
        if (headervalue == null || headervalue.trim().equals("")) {
            return null;
        }
        return headervalue.trim();
    }

}
