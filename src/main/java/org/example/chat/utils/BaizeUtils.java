package org.example.chat.utils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import org.example.chat.bean.baize.LoginResult;
import org.example.utils.HttpUtils;
import org.example.utils.JsonUtils;
import org.example.utils.bean.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaizeUtils {

    private static Map<String, String> getBaizeHttpPostHeaderMap(String token, String strParam) {
        return new HashMap<String, String>() {{
//            put("Host", "192.168.23.85:8860");
            put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:139.0) Gecko/20100101 Firefox/139.0");
            put("Accept", "*/*");
            put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
            put("Accept-Encoding", "gzip, deflate");
            put("Content-Type", "application/json");
            put("Content-Length", String.valueOf(strParam.getBytes(StandardCharsets.UTF_8).length));
//            put("Origin", "http://192.168.23.85");
            put("Connection", "keep-alive");
//            put("Referer", "http://192.168.23.85/");
            put("token", token);
            put("Priority", "u=0");
            put("Pragma", "no-cache");
            put("Cache-Control", "no-cache");
        }};
    }

    private static Map<String, String> getBaizeHttpGetHeaderMap(String token) {
        return new HashMap<String, String>() {{
//            put("Host", "192.168.23.85:8860");
            put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:139.0) Gecko/20100101 Firefox/139.0");
            put("Accept", "*/*");
            put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
            put("Accept-Encoding", "gzip, deflate");
            put("Content-Type", "application/json");
//            put("Content-Length", String.valueOf(strParam.getBytes(StandardCharsets.UTF_8).length));
//            put("Origin", "http://192.168.23.85");
            put("Connection", "keep-alive");
//            put("Referer", "http://192.168.23.85/");
            put("token", token);
            put("Priority", "u=0");
            put("Pragma", "no-cache");
            put("Cache-Control", "no-cache");
        }};
    }

    public static LoginResult getLoginResult(String url, String account, String password) {
        Map<String, String> paramMap = ImmutableMap.of("account", account, "password", password);
        HttpResponse rsp = HttpUtils.doMultiPartFormPartPost(url, paramMap, Collections.emptyMap());
        if (rsp.getCode() == 200) {
            try {
                LoginResult loginResult = JsonUtils.fromJson(rsp.getText(), LoginResult.class);
                return loginResult;
            } catch (Exception e) {
                MsgUtils.sendQiweiWarning("BaizeUtils getLoginResult error: code " + rsp.getCode());
                return null;
            }

        }
        return null;
    }

    public static HttpResponse doPost(String url, String token, Object paramObject) {
        String strParam;
        if (paramObject instanceof String) {
            strParam = (String) paramObject;
        } else {
            strParam = JsonUtils.toJson(paramObject, false);
        }
        return HttpUtils.doPost(url, strParam, getBaizeHttpPostHeaderMap(token, strParam));
    }

    public static HttpResponse doPost(String url, String token, Object paramObject, int connectTimeout, int readTimeout) {
        String strParam;
        if (paramObject instanceof String) {
            strParam = (String) paramObject;
        } else {
            strParam = JsonUtils.toJson(paramObject, false);
        }
        return HttpUtils.doPost(url, strParam, getBaizeHttpPostHeaderMap(token, strParam), connectTimeout, readTimeout);
    }

    public static HttpResponse doGet(String url, String token, Map<String, Object> paramMap) {
        return HttpUtils.doGet(url, paramMap, getBaizeHttpGetHeaderMap(token));
    }

    public static HttpResponse doGet(String url, String token, Map<String, Object> paramMap, int connectTimeout, int readTimeout) {
        return HttpUtils.doGet(url, paramMap, getBaizeHttpGetHeaderMap(token), connectTimeout, readTimeout);
    }

    public static boolean download(String url, String token, String pathOutput) {
        return HttpUtils.download(url, getBaizeHttpGetHeaderMap(token), pathOutput);
    }

    public static void main(String[] args) {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwIiwidXNlcklkIjoiMSIsIm5hbWUiOiJhZG1pbiIsInJvbGUiOiIxIiwiZXhwIjoxNzU2MjU2ODEyfQ.I2lQ_Piry49C0XEYMjWNWPx2jdndO-5J6l5GOCM6uHQL7aWnCCZobIWZS2plK46noVRQZL3NFXv3sMSl3MWHf5IG4jwcK2LH23pjYrL76tIhcyyUNjwAXqJYwiZky8WaQE2GtoGSoaNFTufc4RZLqTdzRPVuOi6KNhjnCDFM_gw";
//        HttpUtils.download("http://192.168.23.85:8860/market/AiSpeech/scriptCorpus/downloadAudioFiles", "[25979]", getBaizeHttpGetHeaderMap(token),"data/baize.wav");
    }
}
