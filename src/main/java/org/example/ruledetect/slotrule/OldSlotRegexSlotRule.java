//package org.example.ruledetect.slotrule;
//
//import com.google.common.base.Preconditions;
//import com.google.gson.reflect.TypeToken;
//import javafx.util.Pair;
//import org.example.ruledetect.bean.MaskedSlotFilter;
//import org.example.ruledetect.bean.SlotRuleType;
//import org.example.ruledetect.bean.Slot;
//import org.example.utils.*;
//import org.example.utils.bean.Item;
//
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * 正则中的 masked_slot 不能出现在重复项里
// */
//public class OldSlotRegexSlotRule extends AbstractSlotRule {
//
//    private static final String MASKED_SLOT_LEFT_SIDE = ConstUtils.MASKED_SLOT_LEFT_SIDE;
//    private static final String MASKED_SLOT_RIGHT_SIDE = ConstUtils.MASKED_SLOT_RIGHT_SIDE;
//
//    private static final String[] maskTextArray = {"①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩"};
//
//
//    private final List<String> slotNameList;
//    private final String strRule;
//    private final List<String> normedValueList;
//    private final float score;
////    private final Pattern pattern;
//    private final List<MaskedSlotFilter> maskedSlotFilterList;
//    private final List<MaskedSlotFilter> assertionMaskedSlotFilterList; // 位于否定断言中的slot
//    private final String source;
//
//    public OldSlotRegexSlotRule(
//            List<String> slotNameList, Set<String> domainSet, String strRule, List<String> normedValueList,
//            float score, String source) {
//        super(SlotRuleType.SLOT_REGEX, domainSet);
//        Preconditions.checkArgument(slotNameList.size() == normedValueList.size());
//        this.slotNameList = slotNameList;
//        this.strRule = strRule;
//        this.normedValueList = normedValueList;
//        this.score = score;
//        this.source = source;
//
//        List<Item> itemList = BraceExtractor.extract(strRule, MASKED_SLOT_LEFT_SIDE, MASKED_SLOT_RIGHT_SIDE);
//        itemList = itemList.stream().sorted(Comparator.comparing(Item::getStartIndex))
//                .collect(Collectors.toList());
//        this.maskedSlotFilterList = new ArrayList<>();
//        this.assertionMaskedSlotFilterList = new ArrayList<>();
//        NegativeAssertionExtractor negativeAssertionExtractor = new NegativeAssertionExtractor(strRule);
//        for (Item item: itemList) {
//            int start = item.getStartIndex() - MASKED_SLOT_LEFT_SIDE.length();
//            int end = item.getEndIndex() + MASKED_SLOT_RIGHT_SIDE.length();
//            Map<String, String> filterMap = JsonUtils.fromJson("{" + item.getValue() + "}", new TypeToken<Map<String, String>>(){});
//            MaskedSlotFilter filter = new MaskedSlotFilter(start, end, filterMap);
//            if (negativeAssertionExtractor.isPositionInNegativeLookaround(start)) {
//                this.assertionMaskedSlotFilterList.add(filter);
//            } else {
//                this.maskedSlotFilterList.add(filter);
//            }
//        }
////        this.pattern = Pattern.compile(tmpRule);
//    }
//
//    private static String getMaskedText(int maskIndex) {
//        int rest = maskIndex;
//        StringBuilder sb = new StringBuilder();
//        while (rest >= 0) {
//            int index = rest % maskTextArray.length;
//            rest /= maskTextArray.length;
//            sb.append(maskTextArray[index]);
//            if (rest == 0) {
//                break;
//            }
//        }
//        return "@@「『@@" + sb.reverse().toString() + "@@』」@@";
//    }
//
//    @Override
//    protected List<Slot> predict(String query, List<Slot> existedSlotList) {
//        List<Slot> slotList = new ArrayList<>();
//
//        // 找出符合筛选条件的slot候选列表
//        Map<MaskedSlotFilter, List<Slot>> filter2slotList = new HashMap<>();
//        for (MaskedSlotFilter filter: maskedSlotFilterList) {
//            List<Slot> matchedFilterList = new ArrayList<>();
//            filter2slotList.put(filter, matchedFilterList);
//            for (Slot slot: existedSlotList) {
//                if (filter.isMatch(slot)) {
//                    matchedFilterList.add(slot);
//                }
//            }
//        }
//
//        // 找出符合筛选条件的slot候选列表
//        Map<MaskedSlotFilter, List<Slot>> filter2assertionSlotList = new HashMap<>();
//        for (MaskedSlotFilter filter: assertionMaskedSlotFilterList) {
//            List<Slot> matchedFilterList = new ArrayList<>();
//            filter2assertionSlotList.put(filter, matchedFilterList);
//            for (Slot slot: existedSlotList) {
//                if (filter.isMatch(slot)) {
//                    matchedFilterList.add(slot);
//                }
//            }
//        }
//
//        boolean isMatch = true;
//        for (Map.Entry<MaskedSlotFilter, List<Slot>> entry: filter2slotList.entrySet()) {
//            if (entry.getValue().isEmpty()) {
//                isMatch = false;
//                break;
//            }
//        }
//
//        if (isMatch) {
//            // 获取所有可能的情况
//            List<List<Pair<MaskedSlotFilter, Slot>>> filterSlotPairListList = new ArrayList<>();
//            filterSlotPairListList.add(new ArrayList<>()); // 初始化
//            for (Map.Entry<MaskedSlotFilter, List<Slot>> entry: filter2slotList.entrySet()) {
//                MaskedSlotFilter filter = entry.getKey();
//                List<Slot> matchedSlotList = entry.getValue();
//
//                List<List<Pair<MaskedSlotFilter, Slot>>> newFilterSlotPairListList = new ArrayList<>();
//                for (List<Pair<MaskedSlotFilter, Slot>> filterSlotPairList: filterSlotPairListList) {
////                    // 插入一种不放入slot的选项
////                    newFilterSlotPairListList.add(new ArrayList<>(filterSlotPairList));
//                    // 对每个slot做排列组合
//                    for (Slot slot: matchedSlotList) {
//                        List<Pair<MaskedSlotFilter, Slot>> newFilterSlotPairList = new ArrayList<>(filterSlotPairList);
//                        newFilterSlotPairList.add(new Pair<>(filter, slot));
//                        newFilterSlotPairListList.add(newFilterSlotPairList);
//                    }
//                }
//                filterSlotPairListList = newFilterSlotPairListList;
//            }
//
//            // 负向断言的slot强制要求掩码
//            for (List<Pair<MaskedSlotFilter, Slot>> filterSlotPairList: filterSlotPairListList) {
//                List<Slot> tmpSlotList = filterSlotPairList.stream()
//                        .map(x -> x.getValue()).collect(Collectors.toList());
//                for (Map.Entry<MaskedSlotFilter, List<Slot>> entry: filter2assertionSlotList.entrySet()) {
//                    MaskedSlotFilter filter = entry.getKey();
//                    List<Slot> assertionSlotList = entry.getValue();
//                    for (Slot assertionSlot: assertionSlotList) {
//                        if (!tmpSlotList.contains(assertionSlot)) {
//                            filterSlotPairList.add(new Pair<>(filter, assertionSlot));
//                        }
//                    }
//                }
//            }
//
//            // 对每个情况进行slot识别
//            for (List<Pair<MaskedSlotFilter, Slot>> filterSlotPairList: filterSlotPairListList) {
//                // 同一个位置可能有多个不同的slot，或是同一个slot对应了多个不同的filter（包括出现在或中，但不能出现在前向断言、后向断言中）
//                // 将相同位置的slot合并，以slot的start、end为slotKey
//                Map<String, List<MaskedSlotFilter>> slotKey2filterList = new HashMap<>();
//                Map<String, List<Slot>> slotKey2SlotList = new HashMap<>();
//                Map<String, Pair<Integer, Integer>> slotKey2IndexPair = new HashMap<>();
//                for (Pair<MaskedSlotFilter, Slot> filterSlotPair : filterSlotPairList) {
//                    MaskedSlotFilter filter = filterSlotPair.getKey();
//                    Slot slot = filterSlotPair.getValue();
//                    String slotKey = slot.getStartIndex() + "_" + slot.getEndIndex();
//                    Pair<Integer, Integer> integerPair = new Pair<>(slot.getStartIndex(), slot.getEndIndex());
//
//                    slotKey2filterList.putIfAbsent(slotKey, new ArrayList<>());
//                    slotKey2filterList.get(slotKey).add(filter);
//
//                    slotKey2SlotList.putIfAbsent(slotKey, new ArrayList<>());
//                    slotKey2SlotList.get(slotKey).add(slot);
//
//                    slotKey2IndexPair.put(slotKey, integerPair);
//                }
//
//                // 获取 maskedQuery
//                String maskedQuery = query;
//                Map<Integer, Integer> index2offset = new HashMap<>();
//                List<String> slotKeyList = slotKey2filterList.keySet().stream()
//                        .sorted(Comparator.comparing(x -> slotKey2IndexPair.get(x).getKey())
//                                .thenComparing(x -> slotKey2IndexPair.get(x).getValue()))
//                        .collect(Collectors.toList());
//                int totalOffset = 0;
//                boolean isSkip = false;
//                Map<MaskedSlotFilter, String> filter2MaskText = new HashMap<>();
//                int maskIndex = 0;
//                for (String slotKey: slotKeyList) {
//                    List<MaskedSlotFilter> filterList = slotKey2filterList.get(slotKey);
//                    Slot slot = slotKey2SlotList.get(slotKey).get(0);
//                    String rawValue = slot.getRawValue();
//                    int start = slot.getStartIndex() + totalOffset;
//                    int end = slot.getEndIndex() + totalOffset;
//                    if (maskedQuery.substring(start, end).equals(rawValue)) {
//                        maskIndex += 1;
//                        String maskedText = getMaskedText(maskIndex);
//                        for (MaskedSlotFilter filter: filterList) {
//                            filter2MaskText.put(filter, maskedText);
//                        }
//                        maskedQuery = maskedQuery.substring(0, start) + maskedText + maskedQuery.substring(end);
//                        int offset = maskedText.length() - rawValue.length();
//                        totalOffset += offset;
//                        index2offset.put(end + offset, totalOffset);
//                    } else {
//                        isSkip = true;
//                    }
//                }
//
//                if (isSkip) {
//                    continue;
//                }
//
////                List<MaskedSlotFilter> filterList = maskedSlotFilterList.stream()
//                List<MaskedSlotFilter> filterList = Stream.concat(maskedSlotFilterList.stream(), assertionMaskedSlotFilterList.stream())
//                        .sorted(Comparator.comparing(x -> x.getStartIndex()))
//                        .collect(Collectors.toList());
//                String tmpRule = strRule;
//                int offset = 0;
//                for (MaskedSlotFilter filter: filterList) {
//                    int start = filter.getStartIndex() + offset;
//                    int end = filter.getEndIndex() + offset;
//                    String maskedText;
//                    if (filter2MaskText.containsKey(filter)) {
//                        maskedText = filter2MaskText.get(filter);
//                    } else {
//                        maskIndex += 1;
//                        maskedText = getMaskedText(maskIndex);
//                        filter2MaskText.put(filter, maskedText);
//                    }
//                    tmpRule = tmpRule.substring(0, start) + maskedText + tmpRule.substring(end);
//                    offset += maskedText.length() - (end - start);
//                }
//
//                Matcher matcher = Pattern.compile(tmpRule).matcher(maskedQuery); // compile 方法不传入flag参数，不允许跨行匹配
//                while (matcher.find()) {
//                    String uuid = UUID.randomUUID().toString().replace("-", "");
//                    if (matcher.groupCount() == slotNameList.size()) {
//                        for (int i = 1; i <= matcher.groupCount(); i++) {
//                            String slotName = slotNameList.get(i - 1);
//                            String normedValue = normedValueList.get(i - 1);
//                            int start = matcher.start(i);
//                            int end = matcher.end(i);
//                            int startOffset = 0;
//                            int endOffset = 0;
//                            for (Map.Entry<Integer, Integer> entry: index2offset.entrySet()) {
//                                int tmpOffset = entry.getValue();
//                                if (entry.getKey() <= start && tmpOffset > startOffset) {
//                                    startOffset = tmpOffset;
//                                }
//                                if (entry.getKey() <= end && tmpOffset > endOffset) {
//                                    endOffset = tmpOffset;
//                                }
//                            }
//                            int originalStart = start - startOffset;
//                            int originalEnd = end - endOffset;
//                            String rawValue = query.substring(originalStart, originalEnd);
//                            if (normedValue == null) {
//                                normedValue = rawValue;
//                            }
////                            System.out.println(maskedQuery.substring(start, end) + "\t" + rawValue);
//                            Slot slot = new Slot(slotName, rawValue, normedValue, originalStart, originalEnd, score, source, source + "_" + uuid);
//                            slotList.add(slot);
//                        }
//                    }
//                }
//            }
//        }
//        return slotList;
//    }
//
//    public static void main(String[] args) {
//        System.out.println(getMaskedText(19));
//    }
//}
