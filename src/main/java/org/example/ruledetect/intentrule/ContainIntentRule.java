package org.example.ruledetect.intentrule;

import org.example.ruledetect.bean.IntentRuleType;
import org.example.ruledetect.bean.QueryType;

import java.util.Set;

public class ContainIntentRule extends AbstractIntentRule {

    public ContainIntentRule(String intent, String strRule, QueryType queryType, Set<String> domainSet, float score) {
        super(intent, strRule, IntentRuleType.CONTAIN, queryType, domainSet, score);
    }

    protected boolean match(String query) {
        return query.contains(getStrRule());
    }
}
