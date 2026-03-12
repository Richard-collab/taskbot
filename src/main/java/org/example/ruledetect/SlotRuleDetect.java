package org.example.ruledetect;

import com.google.gson.reflect.TypeToken;
import org.example.ruledetect.bean.*;
import org.example.ruledetect.slotrule.*;
import org.example.utils.JsonUtils;
import org.example.utils.ResourceUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SlotRuleDetect {

    private static final String SLOT_RULES_PATH = ResourceUtils.getAbsolutePath("/slot_rules.json");

    private static final List<AbstractSlotRule> slotRuleList = getSlotRules();
    private static final Map<String, List<AbstractSlotRule>> customSlotRuleListMap = new HashMap<>();


    public static List<SlotRuleBean> getSlotRuleBeanList() {
        return SlotRuleBean.getRulesFromJsonFile(SLOT_RULES_PATH);
    }

    private static List<AbstractSlotRule> getSlotRules() {
        final List<SlotRuleBean> ruleBeans = getSlotRuleBeanList();
        List<AbstractSlotRule> allSlotRuleList = new ArrayList<>();
        Map<String, List<Keyword>> domainSetJson2keywordList = new HashMap<>();
        for (SlotRuleBean ruleBean: ruleBeans) {
            SlotRuleType ruleType = ruleBean.getRuleType();
            Set<String> domainSet = ruleBean.getDomainSet();
            if (ruleType == SlotRuleType.WHITELIST) {
                for (String strRule : ruleBean.getStrRuleList()) {
                    String rawValue = strRule;
                    String[] tmp = ruleBean.getSlotName().split(":", 0);
                    String slotName = tmp[0];
                    String normedValue = null;
                    if (tmp.length == 2) {
                        normedValue = tmp[1];
                    }
                    Keyword keyword = new Keyword(slotName, rawValue, normedValue, ruleBean.getScore(), ruleBean.getSource());
                    String domainSetJson = getDomainSetJson(domainSet);
                    domainSetJson2keywordList.putIfAbsent(domainSetJson, new ArrayList<>());
                    domainSetJson2keywordList.get(domainSetJson).add(keyword);
                }
            } else if (ruleType == SlotRuleType.REGEX || ruleType == SlotRuleType.SLOT_REGEX) {
                //用英文分号;分割不同的slot，用英文冒号:分割slotName和normedValue，用竖线|表示不同slot合用同一个括号提取
                List<String> slotNameList = new ArrayList<>();
                List<String> normedValuedList = new ArrayList<>();
                RegexMode regexMode = ruleBean.getRegexMode();
                String[] items = ruleBean.getSlotName().split(";", 0);
                for (String item: items) {
                    String[] tmp = item.split(":", 0);
                    String slotName = tmp[0];
                    String normedValue = null;
                    if (tmp.length == 2) {
                        normedValue = tmp[1];
                    }
                    slotNameList.add(slotName);
                    normedValuedList.add(normedValue);
                }
                for (String strRule : ruleBean.getStrRuleList()) {
                    AbstractSlotRule rule;
                    if (ruleType == SlotRuleType.REGEX) {
                        rule = new RegexSlotRule(slotNameList, regexMode, domainSet, strRule, normedValuedList, ruleBean.getScore(), ruleBean.getSource());
                    } else {
//                        rule = new SlotRegexSlotRule(slotNameList, domainSet, strRule, normedValuedList, ruleBean.getScore(), ruleBean.getSource());
                        rule = new SlotRegexSlotRule(slotNameList, regexMode, domainSet, strRule, normedValuedList, ruleBean.getScore(), ruleBean.getSource());
                    }
                    allSlotRuleList.add(rule);
                }
            }
        }
        for (Map.Entry<String, List<Keyword>> entry: domainSetJson2keywordList.entrySet()) {
            String domainSetJson = entry.getKey();
            List<Keyword> keywordList = entry.getValue();
            Set<String> domainSet = JsonUtils.fromJson(domainSetJson, new TypeToken<Set<String>>(){});
            AbstractSlotRule whitelistSLotRule = new WhitelistSlotRule(keywordList, domainSet);
            allSlotRuleList.add(whitelistSLotRule);
        }
        return allSlotRuleList;
    }

    private static String getDomainSetJson(Set<String> domainSet) {
        List<String> domainList = domainSet.stream()
                .sorted(Comparator.comparing(domain -> domain)).collect(Collectors.toList());
        return JsonUtils.toJson(domainList, false);
    }

    public static void resetCustomSlotRuleList(String key, List<AbstractSlotRule> ruleList) {
        customSlotRuleListMap.put(key, ruleList);
    }

    public static List<Slot> predict(QueryInfo queryInfo) {
        String domain = queryInfo.getDomain();
        List<AbstractSlotRule> targetSlotRuleList = slotRuleList.stream()
                .filter(slotRule -> slotRule.matchDomain(domain))
                .collect(Collectors.toCollection(ArrayList::new));
        customSlotRuleListMap.forEach((key, tmpSlotRuleList) -> {
            List<AbstractSlotRule> tmpTargetSlotRuleList = tmpSlotRuleList.stream()
                    .filter(slotRule -> slotRule.matchDomain(domain))
                    .collect(Collectors.toList());
            targetSlotRuleList.addAll(tmpTargetSlotRuleList);
        });
        List<Slot> slotList = targetSlotRuleList.stream()
                .filter(slot -> slot.getRuleType() != SlotRuleType.SLOT_REGEX)
                .flatMap(rule -> rule.predict(queryInfo, Collections.emptyList()).stream())
                .collect(Collectors.toList());
        List<Slot> mergedIndependentSlotList = SlotMerge.mergeSlots(slotList);
        List<Slot> allSlotList = new ArrayList<>(mergedIndependentSlotList);
        List<Slot> dependentSlotList;
        do {
            dependentSlotList = targetSlotRuleList.stream()
                    .filter(rule -> rule.getRuleType() == SlotRuleType.SLOT_REGEX)
                    .flatMap(rule -> rule.predict(queryInfo, allSlotList).stream())
                    .collect(Collectors.toList());
            Set<String> allSlotRuleKeySet = allSlotList.stream()
                    .map(slot -> slot.getKey()).collect(Collectors.toSet());
            dependentSlotList = dependentSlotList.stream()
                    .filter(slot -> !allSlotRuleKeySet.contains(slot.getKey()))
                    .collect(Collectors.toList());
            allSlotList.addAll(dependentSlotList);
        } while (dependentSlotList.size() > 0); // 只要有新slot出现，就继续执行 SLOT_REGEX rule
//        List<Slot> dependentSlotList = targetSlotRuleList.stream()
//                .filter(slot -> slot.getRuleType() == SlotRuleType.SLOT_REGEX)
//                .flatMap(rule -> rule.predict(queryInfo, mergedIndependentSlotList).stream())
//                .collect(Collectors.toList());
//        dependentSlotList = SlotMerge.mergeSlots(dependentSlotList);
//        List<Slot> allSlotList = new ArrayList<>();
//        allSlotList.addAll(dependentSlotList);
//        allSlotList.addAll(mergedIndependentSlotList);
        return SlotMerge.mergeSlots(allSlotList);
    }

    public static void main(String[] args) {
//        String query = "分期乐，得心FQL707线路并发调整到650";
//        String query = "weiqianbao333，使用仙人077线路的任务并发调整到1000";
//        String query = "账号：baotaitestai，启动任务，仙人线路，10并发";
//        String query = "weiqianbao333，仙人jryi077线路的任务并发调整到1000";
//        String query = "15138655621，15082057813要下7.8录音";
////        String query = "17703914615        2025-07-18 16:13:04";
//        String query = "吉用花提交的用标注的线路呼哈，仙人限时并发都先开500，9点开始哈";
//        String query = "吉用花提交的用标注的线路呼哈，仙人jrsan001限时jr002并发都先开500，9点开始哈";
//        String query = "产品名称：11\n1";
//        String query = "白泽ip：192.168.106.208|192.168.106.201\n";
//        String query = "yingdian888,043线路并发调整到3000";
//        String query = "用043外呼";
//        String query = "吉用花公众号任务对比3-限时-10点\n吉用花公众号任 务对比5-限时-10点\n吉用花公众号任务对比1-限时-10点\n吉用花公众号任务对比11-限时-10点\n吉用花公众号任务对比9-限时-10点\n 吉用花公众号任务对比7-限时-10点";
//        String query = "以-_AA结尾的任务开呼";
//        String query = "后缀为“3”的任务";
//        String query = "C1_PT1_fc44b1d5-ec60-44c9-86d5-96e130cf3ded";
//        String query = "话后回调接口（新）、文本补推接口：http://jxt.goutbound.status.jinxintian.cn/goutbound-call-status-push/statusPush/outbound/baize2/task/66201";
//        String query = "langshun06：上海 天津 成都 贵阳 长沙 西安 深圳 济宁 沈阳 济南 百色 潍坊 加入全天屏蔽";
//        String query = "2026011202线路调整为600并发";
//        String query = "操作类型：恢复任务\n" +
//                "主账号：chen001、baotaitestai\n" +
//                "任务类型：AI外呼、人机协同\n" +
//                "自动停止任务：包含\n" +
//                "止损任务：包含\n" +
//                "任务状态：未完成、已停止\n" +
//                "任务创建时间：上午\n" +
//                "任务名称：以333结尾的任务";
        String query = "chen001 操作：调整并发 jrdt001线路的任务 改用jrdt001线路 20并发";
        String domain = Domain.SHANGHUYUNYING;
        QueryInfo queryInfo = QueryInfo.createQueryInfo(domain, query);
        List<Slot> slotList = predict(queryInfo);
        System.out.println(slotList);
    }
}
