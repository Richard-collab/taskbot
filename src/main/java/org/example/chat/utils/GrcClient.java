package org.example.chat.utils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import org.example.instruction.bean.ExecutionResult;
import org.example.utils.CollectionUtils;
import org.example.utils.HttpUtils;
import org.example.utils.JsonUtils;
import org.example.utils.bean.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GrcClient {

    private static final String BASE_URL = "http://127.0.0.1:5000";

    private static Map<String, String> getHttpPostHeaderMap(String strParam) {
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
            put("Priority", "u=0");
            put("Pragma", "no-cache");
            put("Cache-Control", "no-cache");
        }};
    }

    private static HttpResponse doPost(String url, Object paramObject) {
        String strParam;
        if (paramObject instanceof String) {
            strParam = (String) paramObject;
        } else {
            strParam = JsonUtils.toJson(paramObject, false);
        }
        return HttpUtils.doPost(url, strParam, getHttpPostHeaderMap(strParam));
    }


    public static ExecutionResult reportTaskStatistic(
            String account, String strDate, List<String> intentionClassList, boolean needAvgCallDuration, boolean needTotalCallDuration) {
        String url = BASE_URL + "/taskReport";

        Map<String, Object> paramMap = ImmutableMap.of(
                "account", account,
                "call_date", strDate,
                "intention_class_list", intentionClassList,
                "need_avg_call_duration", needAvgCallDuration,
                "need_total_call_duration", needTotalCallDuration
        );

        HttpResponse response = doPost(url, paramMap);

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                String code = baseObj.get("code").getAsString();
                String msg = baseObj.get("msg").getAsString();
                boolean flag = "2000".equals(code);
                return new ExecutionResult(flag, msg);
            } catch (Exception e) {
                return new ExecutionResult(false, e.toString());
            }
        } else {
            String msg = "http status code: " + response.getCode();
            return new ExecutionResult(false, msg);
        }
    }


    public static ExecutionResult reportTaskScriptStatistic(String account, String strDate, List<String> taskNameContainList) {
        String url = BASE_URL + "/fetchTasksInfo";

        Map<String, Object> paramMap = ImmutableMap.of(
                "account", account,
                "call_date", strDate,
                "keyword_list", taskNameContainList
        );

        HttpResponse response = doPost(url, paramMap);

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                String code = baseObj.get("code").getAsString();
                String msg = baseObj.get("msg").getAsString();
                boolean flag = "2000".equals(code);
                return new ExecutionResult(flag, msg);
            } catch (Exception e) {
                return new ExecutionResult(false, e.toString());
            }
        } else {
            String msg = "http status code: " + response.getCode();
            return new ExecutionResult(false, msg);
        }
    }


    public static ExecutionResult reportPhoneRecord(String strStartDate, String strEndDate, String contentContain, Set<String> intentSet, String lineCodeContain) {
        String url = null;
        Map<String, Object> paramMap = null;

        if (CollectionUtils.isEmpty(intentSet)) {
            url = BASE_URL + "/fetchAbnormalRecord";
            paramMap = new HashMap<>();
            paramMap.put("start_call_date", strStartDate);
            paramMap.put("end_call_date", strEndDate);
            paramMap.put("keyword", contentContain);
            paramMap.put("line_code_keyword", lineCodeContain);
        } else {
            url = BASE_URL + "/fetchAbnormalRecordFromSemantic";
            paramMap = new HashMap<>();
            paramMap.put("start_call_date", strStartDate);
            paramMap.put("end_call_date", strEndDate);
            paramMap.put("semantic", intentSet);
            paramMap.put("line_code_keyword", lineCodeContain);
        }

        HttpResponse response = doPost(url, paramMap);

        if (response.getCode() == 200) {
            try {
                JsonObject baseObj = JsonUtils.parseJson(response.getText()).getAsJsonObject();
                String code = baseObj.get("code").getAsString();
                String msg = baseObj.get("msg").getAsString();
                boolean flag = "2000".equals(code);
                return new ExecutionResult(flag, msg);
            } catch (Exception e) {
                return new ExecutionResult(false, e.toString());
            }
        } else {
            String msg = "http status code: " + response.getCode();
            return new ExecutionResult(false, msg);
        }
    }
}
