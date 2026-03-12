package org.example.ruledetect.intentrule;

import org.example.ruledetect.bean.IntentRuleType;
import org.example.ruledetect.bean.QueryType;

import java.util.Set;

public class WhitelistIntentRule extends AbstractIntentRule {

    public WhitelistIntentRule(String intent, String strRule, QueryType queryType, Set<String> domainSet, float score) {
        super(intent, strRule, IntentRuleType.WHITELIST, queryType, domainSet, score);
    }

    protected boolean match(String query) {
        return getStrRule().equals(query);
    }
}
