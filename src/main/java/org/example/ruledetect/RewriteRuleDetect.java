package org.example.ruledetect;

import org.example.ruledetect.bean.RewriteRuleBean;
import org.example.ruledetect.rewriterule.RewriteRule;
import org.example.utils.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

public class RewriteRuleDetect {

    private static final String REWRITE_RULES_PATH = ResourceUtils.getAbsolutePath("/rewrite_rules.json");

    private static List<RewriteRule> rewriteRuleList;

    static {
        init();
    }

    public static void init() {
        rewriteRuleList = getRewriteRules(REWRITE_RULES_PATH);
    }

    private static List<RewriteRule> getRewriteRules(String pathInputJson) {
        List<RewriteRuleBean> ruleBeans = RewriteRuleBean.getRulesFromJsonFile(pathInputJson);
        List<RewriteRule> allRewriteRuleList = new ArrayList<>();
        for (RewriteRuleBean ruleBean: ruleBeans) {
            for (String strRule: ruleBean.getStrRuleList()) {
                RewriteRule rewriteRule = new RewriteRule(strRule);
                allRewriteRuleList.add(rewriteRule);
            }
        }
        return allRewriteRuleList;
    }

    public static String rewrite(String query) {
        String resultQuery = query;
        for (RewriteRule rewriteRule: rewriteRuleList) {
            resultQuery = rewriteRule.rewrite(resultQuery);
        }
        return resultQuery;
    }

    public static void main(String[] args) {
        System.out.println(rewrite("这是  什么呢"));
    }
}
