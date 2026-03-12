package org.example.ruledetect.bean;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import org.example.utils.JsonUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class IntentRuleBean implements Serializable {

    private String intent = null;
    private List<String> strRuleList = Collections.emptyList();
    private QueryType queryType = QueryType.RAW_QUERY;
    private IntentRuleType ruleType = null;
    private Set<String> domainSet = Sets.newHashSet(Domain.ALL);
    private float score = -1;

    public String getIntent() {
        return intent;
    }

    public List<String> getStrRuleList() {
        return strRuleList;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public IntentRuleType getRuleType() {
        return ruleType;
    }

    public Set<String> getDomainSet() {
        return domainSet;
    }

    public float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

    public boolean matchDomain(String domain) {
        return Domain.match(domainSet, domain);
    }

    public static List<IntentRuleBean> getRulesFromJsonFile(String pathInputJson) {
        List<IntentRuleBean> rules = null;
        try {
            String text = new String(Files.readAllBytes(Paths.get(pathInputJson)), StandardCharsets.UTF_8);
            rules = JsonUtils.fromJson(text, new TypeToken<List<IntentRuleBean>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rules;
    }

    public static List<IntentRuleBean> getRulesFromJsonFileList(List<String> pathInputJsonList) {
        List<IntentRuleBean> allRuleList = new ArrayList<>();
        for (String pathInputJson: pathInputJsonList) {
            List<IntentRuleBean> ruleList = getRulesFromJsonFile(pathInputJson);
            allRuleList.addAll(ruleList);
        }
        return allRuleList;
    }
}
