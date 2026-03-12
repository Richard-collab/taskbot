package org.example.ruledetect.intentrule;

import org.example.ruledetect.bean.Domain;
import org.example.ruledetect.bean.IntentRuleType;
import org.example.ruledetect.bean.QueryInfo;
import org.example.ruledetect.bean.QueryType;
import org.example.utils.JsonUtils;

import java.util.Set;

public abstract class AbstractIntentRule {

    private final String intent;
    private final String strRule;
    private final IntentRuleType ruleType;
    private final QueryType queryType;
    private final Set<String> domainSet;
    private final float score;

    public AbstractIntentRule(
            String intent, String strRule, IntentRuleType ruleType, QueryType queryType, Set<String> domainSet, float score) {
        this.intent = intent;
        this.strRule = strRule;
        this.ruleType = ruleType;
        this.queryType = queryType;
        this.domainSet = domainSet;
        this.score = score;
    }

    public boolean match(QueryInfo queryInfo) {
        if (matchDomain(queryInfo.getDomain())) {
            String query = getQuery(queryInfo);
            return match(query);
        } else {
            return false;
        }
    }

    protected abstract boolean match(String query);

    public String getQuery(QueryInfo queryInfo) {
        return queryInfo.getQuery(queryType);
    }

    public String getIntent() {
        return intent;
    }

    public String getStrRule() {
        return strRule;
    }

    public IntentRuleType getRuleType() {
        return ruleType;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public Set<String> getDomainSet() {
        return domainSet;
    }

    public boolean matchDomain(String domain) {
        return Domain.match(this.getDomainSet(), domain);
    }

    public float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
