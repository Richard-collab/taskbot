package org.example.ruledetect.bean;

import com.google.gson.reflect.TypeToken;
import org.example.utils.JsonUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class RewriteRuleBean implements Serializable {

    private List<String> strRuleList = Collections.emptyList();

    public List<String> getStrRuleList() {
        return strRuleList;
    }


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

    public static List<RewriteRuleBean> getRulesFromJsonFile(String pathInputJson) {
        List<RewriteRuleBean> rules = null;
        try {
            String text = new String(Files.readAllBytes(Paths.get(pathInputJson)), StandardCharsets.UTF_8);
            rules = JsonUtils.fromJson(text, new TypeToken<List<RewriteRuleBean>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rules;
    }
}
