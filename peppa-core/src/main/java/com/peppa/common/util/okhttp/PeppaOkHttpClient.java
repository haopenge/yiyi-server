package com.peppa.common.util.okhttp;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


public class PeppaOkHttpClient {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();

    public static String postForm(String url, Map<String, String> formMap) throws IOException {
        Set<Map.Entry<String, String>> entries = formMap.entrySet();
        FormBody.Builder builder = new FormBody.Builder();
        entries.forEach(map -> builder.add((String) map.getKey(), String.valueOf(map.getValue())));


        FormBody formBody = builder.build();
        Request request = (new Request.Builder()).url(url).post((RequestBody) formBody).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("unexpected code " + response);

            return response.body().string();
        }
    }


    @Deprecated
    public static String get(String url, Map<String, String> dataMap) throws IOException {
        Set<Map.Entry<String, String>> entries = dataMap.entrySet();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(url).append("?");
        entries.forEach(map -> {
            if (stringBuffer.toString().indexOf("&") >= 0)
                stringBuffer.append("&");
            stringBuffer.append((String) map.getKey()).append("=").append(String.valueOf(map.getValue()));
        });
        Request request = (new Request.Builder()).url(stringBuffer.toString()).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    public static String getByMap(String url, Map<String, String> dataMap) throws IOException {
        Set<Map.Entry<String, String>> entries = dataMap.entrySet();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(url);
        if (dataMap != null && dataMap.size() > 0) {
            if (stringBuffer.toString().indexOf("?") < 0)
                stringBuffer.append("?");
            entries.forEach(map -> {
                if (stringBuffer.toString().indexOf("=") >= 0)
                    stringBuffer.append("&");
                stringBuffer.append((String) map.getKey()).append("=").append(String.valueOf(map.getValue()));
            });
        }
        Request request = (new Request.Builder()).url(stringBuffer.toString()).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    public static String postBody(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);


        Request request = (new Request.Builder()).url(url).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}

