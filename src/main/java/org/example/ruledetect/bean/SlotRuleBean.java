package org.example.ruledetect.bean;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import org.example.utils.JsonUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SlotRuleBean implements Serializable {

    private String slotName = "";
    private List<String> strRuleList = Collections.emptyList();
    private SlotRuleType ruleType = null;
    private RegexMode regexMode = RegexMode.DEFAULT;
    private Set<String> domainSet = Sets.newHashSet(Domain.ALL);
    private float score = -1;
    private String source = "";

    public SlotRuleBean() {
    }

    public SlotRuleBean(String slotName, List<String> strRuleList, SlotRuleType ruleType, RegexMode regexMode, float score, String source) {
        this.slotName = slotName;
        this.strRuleList = strRuleList;
        this.ruleType = ruleType;
        this.regexMode = (regexMode == null)? RegexMode.DEFAULT: regexMode;
        this.score = score;
        this.source = source;
    }

    public String getSlotName() {
        return slotName;
    }

    public List<String> getStrRuleList() {
        return strRuleList;
    }

    public SlotRuleType getRuleType() {
        return ruleType;
    }

    public RegexMode getRegexMode() {
        return regexMode;
    }

    public Set<String> getDomainSet() {
        return domainSet;
    }

    public float getScore() {
        return score;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

    public static List<SlotRuleBean> getRulesFromJsonFile(String pathInputJson) {
        List<SlotRuleBean> rules = null;
        try {
            rules = JsonUtils.fromJsonFile(pathInputJson, new TypeToken<List<SlotRuleBean>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rules;
    }
}
