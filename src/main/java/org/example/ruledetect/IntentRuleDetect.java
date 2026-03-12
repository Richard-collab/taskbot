package org.example.ruledetect;

import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.example.ruledetect.bean.*;
import org.example.ruledetect.intentrule.AbstractIntentRule;
import org.example.ruledetect.intentrule.ContainIntentRule;
import org.example.ruledetect.intentrule.RegexIntentRule;
import org.example.ruledetect.intentrule.WhitelistIntentRule;
import org.example.utils.CollectionUtils;
import org.example.utils.ResourceUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntentRuleDetect {

    public static final List<String> INTENT_RULES_PATH_LIST = Arrays.asList(
            ResourceUtils.getAbsolutePath("/intent_rules.json")
    );

//    private static final String LABEL_OTHER = IntentDetect.LABEL_OTHER;
    private static final Function<AbstractIntentRule, Integer> FUNC_GET_RULE_LEN = rule -> rule.getStrRule().length();

    private static List<AbstractIntentRule> regexIntentRuleList = Collections.emptyList();
    private static List<AbstractIntentRule> containIntentRuleList = Collections.emptyList();
    private static Map<String, List<WhitelistIntentRule>> whitelistRulesMap = Collections.emptyMap();

    private static final String ANY = "ANY";
    private static final List<Pair<String, String>> RETAIN_LEFT_INTENT_LIST = Lists.newArrayList(
    );

    static {
        init();
    }

    public static void init() {
        initIntentRules(INTENT_RULES_PATH_LIST);
    }

    private static void initIntentRules(List<String> pathInputJsonList) {
        List<IntentRuleBean> ruleBeans = IntentRuleBean.getRulesFromJsonFileList(pathInputJsonList);
        List<AbstractIntentRule> allRegexRuleList = new ArrayList<>();
        List<AbstractIntentRule> allContainRuleList = new ArrayList<>();
        Map<String, List<WhitelistIntentRule>> allWhitelistRulesMap = new HashMap<>();
        for (IntentRuleBean ruleBean: ruleBeans) {
            if (ruleBean.getRuleType() == IntentRuleType.WHITELIST) {
                for (String strRule : ruleBean.getStrRuleList()) {
                    if (StringUtils.isEmpty(strRule.trim())) {
                        continue;
                    }
                    WhitelistIntentRule rule = new WhitelistIntentRule(
                            ruleBean.getIntent(), strRule, ruleBean.getQueryType(), ruleBean.getDomainSet(), ruleBean.getScore());
                    List<WhitelistIntentRule> ruleList;
                    if (allWhitelistRulesMap.containsKey(strRule)) {
                        ruleList = allWhitelistRulesMap.get(strRule);
                    } else {
                        ruleList = new ArrayList<>();
                        allWhitelistRulesMap.put(strRule, ruleList);
                    }
                    ruleList.add(rule);
                }
            } else if (ruleBean.getRuleType() == IntentRuleType.CONTAIN) {
                for (String strRule : ruleBean.getStrRuleList()) {
                    if (StringUtils.isEmpty(strRule.trim())) {
                        continue;
                    }
                    AbstractIntentRule rule = new ContainIntentRule(
                            ruleBean.getIntent(), strRule, ruleBean.getQueryType(), ruleBean.getDomainSet(), ruleBean.getScore());
                    allContainRuleList.add(rule);
                }
            } else if (ruleBean.getRuleType() == IntentRuleType.REGEX) {
                for (String strRule : ruleBean.getStrRuleList()) {
                    if (StringUtils.isEmpty(strRule.trim())) {
                        continue;
                    }
                    AbstractIntentRule rule = new RegexIntentRule(
                            ruleBean.getIntent(), strRule, ruleBean.getQueryType(), ruleBean.getDomainSet(), ruleBean.getScore());
                    allRegexRuleList.add(rule);
                }
            }
        }
        // 先按短语长度、再按score从大到小排序
        allContainRuleList.sort(Comparator.comparing(FUNC_GET_RULE_LEN).thenComparing(AbstractIntentRule::getScore).reversed());
        // 按score从大到小排序
        allRegexRuleList.sort(Comparator.comparing(AbstractIntentRule::getScore).reversed());
        whitelistRulesMap = allWhitelistRulesMap;
        containIntentRuleList = allContainRuleList;
        regexIntentRuleList = allRegexRuleList;
    }

    public static List<IntentInfo> predict(QueryInfo queryInfo) {
        List<IntentInfo> intentInfoList = new ArrayList<>();
        String domain = queryInfo.getDomain();
        String query = queryInfo.getRawQuery();
        List<WhitelistIntentRule> hitRuleList = whitelistRulesMap.get(query);
        if (!CollectionUtils.isEmpty(hitRuleList)) {
            for (WhitelistIntentRule rule: hitRuleList) {
                if (rule.matchDomain(domain)) {
                    IntentInfo intentInfo = new IntentInfo(rule.getIntent(), rule.getScore(), rule.getStrRule());
                    intentInfoList.add(intentInfo);
                }
            }
        }
//        if (CollectionUtils.isEmpty(intentInfoList)) {
            for (AbstractIntentRule rule : regexIntentRuleList) {
                if (rule.match(queryInfo)) {
                    IntentInfo intentInfo = new IntentInfo(rule.getIntent(), rule.getScore(), rule.getStrRule());
                    intentInfoList.add(intentInfo);
//                    break; // early stop
                }
            }
            for (AbstractIntentRule rule : containIntentRuleList) {
                if (rule.matchDomain(domain) && rule.match(queryInfo)) {
                    IntentInfo intentInfo = new IntentInfo(rule.getIntent(), rule.getScore(), rule.getStrRule());
                    intentInfoList.add(intentInfo);
//                    break; // early stop
                }
            }
//        }
        // 同时出现时，只保留左侧的意图
        for (Pair<String, String> intentPair: RETAIN_LEFT_INTENT_LIST) {
            String leftIntent = intentPair.getKey();
            String rightIntent = intentPair.getValue();
            Set<String> intentSet = intentInfoList.stream()
                    .map(intentInfo -> intentInfo.getIntent()).collect(Collectors.toSet());
            boolean flag = false;
            if (ANY.equals(leftIntent)) {
                intentSet.remove(rightIntent);
                if (intentSet.size() > 0) {
                    flag = true;
                }
            } else if (intentSet.contains(leftIntent) && intentSet.contains(rightIntent)) {
                flag = true;
            }
            if (flag) {
                for (int i = 0; i < intentInfoList.size(); i++) {
                    if (rightIntent.equals(intentInfoList.get(i).getIntent())) {
                        intentInfoList.remove(i);
                        i--;
                    }
                }
            }
        }
        intentInfoList.sort(Comparator.comparing(IntentInfo::getScore).reversed());
        return intentInfoList;
//        if (CollectionUtils.isEmpty(intentInfoList)) {
////            return new IntentInfo(LABEL_OTHER, -1, RESULT_SOURCE);
//            return null;
//        } else {
//            return intentInfoList;
//        }
    }

    public static List<AbstractIntentRule> getRegexIntentRuleList() {
        return regexIntentRuleList;
    }

    public static List<AbstractIntentRule> getContainIntentRuleList() {
        return containIntentRuleList;
    }

    public static Map<String, List<WhitelistIntentRule>> getWhitelistRulesMap() {
        return whitelistRulesMap;
    }

    public static void main(String[] args) {
//        Creator creator = Creator.Unknown;
//        QueryInfo queryInfo = Preprocessor.getQueryInfo("有没有临时额度", Domain.XINYONGKA);
//        QueryInfo queryInfo = Preprocessor.getQueryInfo("有毛病", Domain.ZHENGQUAN);
//        QueryInfo queryInfo = Preprocessor.getQueryInfo("多少钱", Domain.SHIPIN);
//        QueryInfo queryInfo = Preprocessor.getQueryInfo("好了的", Domain.JIAOPEI);
//        QueryInfo queryInfo = Preprocessor.getQueryInfo("批不下来:;", Domain.YOUXI);
        String domain = Domain.SHANGHUYUNYING;
//        String query = "整体呼到5000就行";
//        String query = "1万2开呼";
//        String query = "极融公众号1点40原线路 补呼一轮";
//        String query = "chen001 并发改到30";
//        String query = "chen001 并发改到70 14:34开始";
//        String query = "15138655621，15082057813要下7.8录音";
//        String query = "随州、咸宁、仙桃、黄石、武汉、石家庄加入屏蔽海花公众号屏蔽";
//        String query = "吉用花公众号任务对比3-限时-10点\n吉用花公众号任 务对比5-限时-10点\n吉用花公众号任务对比1-限时-10点\n吉用花公众号任务对比11-限时-10点\n吉用花公众号任务对比9-限时-10点\n 吉用花公众号任务对比7-限时-10点";
//        String query = "梧州 石家庄 兰州 河池 崇左 宜昌 取消langshun05屏蔽";
//        String query = "操作类型：替换语料名称和内容";
//        String query = "操作类型：恢复任务\n" +
//                "主账号：chen001、baotaitestai\n" +
//                "任务类型：AI外呼、人机协同\n" +
//                "自动停止任务：包含\n" +
//                "止损任务：包含\n" +
//                "任务状态：未完成、已停止\n" +
//                "任务创建时间：上午\n" +
//                "任务名称：以333结尾的任务";
        String query = "chen001 调整并发 jrdt001线路的任务 改用jrdt001线路 20并发";
        QueryInfo queryInfo = QueryInfo.createQueryInfo(domain, query);
        System.out.println(predict(queryInfo));
        System.out.println(regexIntentRuleList.stream().mapToInt(rule -> rule.getStrRule().length()).max());
    }
}
