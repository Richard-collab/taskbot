package org.example.ruledetect.intentrule;

import org.example.ruledetect.bean.IntentRuleType;
import org.example.ruledetect.bean.QueryType;

import java.util.Set;
import java.util.regex.Pattern;

public class RegexIntentRule extends AbstractIntentRule {

    private final Pattern pattern;
    public RegexIntentRule(String intent, String strRule, QueryType queryType, Set<String> domainSet, float score) {
        super(intent, strRule, IntentRuleType.REGEX, queryType, domainSet, score);
        this.pattern = Pattern.compile(strRule, Pattern.DOTALL); // DOTALL 模式下“.”匹配任何字符，包括行换行符、终止符。
    }

    protected boolean match(String query) {
        return pattern.matcher(query).matches();
    }
}
