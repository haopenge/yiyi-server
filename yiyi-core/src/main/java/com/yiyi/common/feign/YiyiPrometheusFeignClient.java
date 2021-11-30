package com.yiyi.common.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class YiyiPrometheusFeignClient implements Client {
    private static final Log log = LogFactory.getLog(YiyiPrometheusFeignClient.class);
    private final Client delegate;

    public YiyiPrometheusFeignClient(Client delegate) {
        this.delegate = delegate;
    }

    public Response execute(Request request, Request.Options options) throws IOException {
        Response response = null;
        try {
            response = this.delegate.execute(request, options);
            if (isDomain(request.url())) {
                int responseCode = response.status();
                count(request, "" + responseCode);
            }
        } catch (IOException exp) {
            count(request, "506");
            throw exp;
        }
        return response;
    }

    private void count(Request request, String code) {
        try {
            String uri = request.url();
            String method = request.method();
            PrometheusFeignCounter.count(method, uri, code);
        } catch (Exception e) {
            log.error("feign 计数异常：", e);
        }
    }

    private boolean isDomain(String strurl) {
        try {
            URL url = new URL(strurl);
            String host = url.getHost();
            if (isIP(host)) {
                return false;
            }
            return true;
        } catch (Exception exception) {


            return false;
        }
    }

    private boolean isIP(String addr) {
        if (addr == null || addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }


        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        return ipAddress;
    }
}


