package org.example.ruledetect.slotrule;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import org.example.ruledetect.bean.MaskedSlotFilter;
import org.example.ruledetect.bean.RegexMode;
import org.example.ruledetect.bean.SlotRuleType;
import org.example.ruledetect.bean.Slot;
import org.example.utils.*;
import org.example.utils.bean.Item;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SlotRegexSlotRule extends AbstractSlotRule {

    private static final String MASKED_SLOT_LEFT_SIDE = ConstUtils.MASKED_SLOT_LEFT_SIDE;
    private static final String MASKED_SLOT_RIGHT_SIDE = ConstUtils.MASKED_SLOT_RIGHT_SIDE;

    private static final String[] maskTextArray = {"①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩"};
    private static final int MAX_SLOT_COUNT = 50000;

    private final List<String> slotNameList;
    private final RegexMode regexMode;
    private final String strRule;
    private final List<String> normedValueList;
    private final float score;
//    private final Pattern pattern;
    private final List<MaskedSlotFilter> maskedSlotFilterList;
    private final List<MaskedSlotFilter> nonCapturingMaskedSlotFilterList; // 非捕获组，可能出现0次或多次
    private final List<MaskedSlotFilter> assertionMaskedSlotFilterList; // 位于否定断言中的slot，query中的对应slot必须全部mask

    private final Map<MaskedSlotFilter, String> filter2MaskText;
    private final String source;

    public SlotRegexSlotRule(
            List<String> slotNameList, RegexMode regexMode, Set<String> domainSet, String strRule, List<String> normedValueList,
            float score, String source) {
        super(SlotRuleType.SLOT_REGEX, domainSet);
        Preconditions.checkArgument(slotNameList.size() == normedValueList.size());
        this.slotNameList = slotNameList;
        this.regexMode = regexMode;
        this.strRule = strRule;
        this.normedValueList = normedValueList;
        this.score = score;
        this.source = source;

        List<Item> itemList = BraceExtractor.extract(strRule, MASKED_SLOT_LEFT_SIDE, MASKED_SLOT_RIGHT_SIDE);
        itemList = itemList.stream().sorted(Comparator.comparing(Item::getStartIndex))
                .collect(Collectors.toList());
        this.maskedSlotFilterList = new ArrayList<>();
        this.assertionMaskedSlotFilterList = new ArrayList<>();
        this.nonCapturingMaskedSlotFilterList = new ArrayList<>();
        this.filter2MaskText = new HashMap<>();
        NegativeAssertionExtractor negativeAssertionExtractor = new NegativeAssertionExtractor(strRule);
        CapturingGroupExtractor capturingGroupExtractor = new CapturingGroupExtractor(strRule);
        int maskIndex = 0;
        for (Item item: itemList) {
            int start = item.getStartIndex() - MASKED_SLOT_LEFT_SIDE.length();
            int end = item.getEndIndex() + MASKED_SLOT_RIGHT_SIDE.length();
            Map<String, String> filterMap = JsonUtils.fromJson("{" + item.getValue() + "}", new TypeToken<Map<String, String>>(){});
            MaskedSlotFilter filter = new MaskedSlotFilter(start, end, filterMap);
            maskIndex++;
            String maskText = getMaskedText(maskIndex);
            this.filter2MaskText.put(filter, maskText);
            if (negativeAssertionExtractor.isPositionInNegativeLookaround(start)) {
                this.assertionMaskedSlotFilterList.add(filter);
            } else if (capturingGroupExtractor.isPositionInCapturingGroup(start)) {
                this.maskedSlotFilterList.add(filter);
            } else {
                this.nonCapturingMaskedSlotFilterList.add(filter);
            }
        }
//        this.pattern = Pattern.compile(tmpRule, regexMode.getFlag());
    }

    private static String getMaskedText(int maskIndex) {
        int rest = maskIndex;
        StringBuilder sb = new StringBuilder();
        while (rest >= 0) {
            int index = rest % maskTextArray.length;
            rest /= maskTextArray.length;
            sb.append(maskTextArray[index]);
            if (rest == 0) {
                break;
            }
        }
        return "@@「『@@" + sb.reverse().toString() + "@@』」@@";
    }

    @Override
    protected List<Slot> predict(String query, List<Slot> existedSlotList) {
        List<Slot> slotList = new ArrayList<>();

        // 找出符合筛选条件的slot候选列表
        Map<MaskedSlotFilter, List<Slot>> filter2slotList = new HashMap<>();
        for (MaskedSlotFilter filter: maskedSlotFilterList) {
            List<Slot> matchedFilterList = new ArrayList<>();
            filter2slotList.put(filter, matchedFilterList);
            for (Slot slot: existedSlotList) {
                if (filter.isMatch(slot)) {
                    matchedFilterList.add(slot);
                }
            }
        }

        // 找出符合筛选条件的slot候选列表
        Map<MaskedSlotFilter, List<Slot>> filter2assertionSlotList = new HashMap<>();
        for (MaskedSlotFilter filter: assertionMaskedSlotFilterList) {
            List<Slot> matchedFilterList = new ArrayList<>();
            filter2assertionSlotList.put(filter, matchedFilterList);
            for (Slot slot: existedSlotList) {
                if (filter.isMatch(slot)) {
                    matchedFilterList.add(slot);
                }
            }
        }

        // 找出符合筛选条件的slot候选列表
        Map<MaskedSlotFilter, List<Slot>> filter2nonCapturingSlotList = new HashMap<>();
        for (MaskedSlotFilter filter: nonCapturingMaskedSlotFilterList) {
            List<Slot> matchedFilterList = new ArrayList<>();
            filter2nonCapturingSlotList.put(filter, matchedFilterList);
            for (Slot slot: existedSlotList) {
                if (filter.isMatch(slot)) {
                    matchedFilterList.add(slot);
                }
            }
        }

        boolean isMatch = true;
        for (Map.Entry<MaskedSlotFilter, List<Slot>> entry: filter2slotList.entrySet()) {
            if (entry.getValue().isEmpty()) {
                isMatch = false;
                break;
            }
        }

        if (isMatch) {
            // 获取所有可能的情况
            List<List<Pair<MaskedSlotFilter, Slot>>> filterSlotPairListList = new ArrayList<>();
            filterSlotPairListList.add(new ArrayList<>()); // 初始化
            for (Map.Entry<MaskedSlotFilter, List<Slot>> entry: filter2slotList.entrySet()) {
                MaskedSlotFilter filter = entry.getKey();
                List<Slot> matchedSlotList = entry.getValue();

                List<List<Pair<MaskedSlotFilter, Slot>>> newFilterSlotPairListList = new ArrayList<>();
                for (List<Pair<MaskedSlotFilter, Slot>> filterSlotPairList: filterSlotPairListList) {
//                    // 插入一种不放入slot的选项
//                    newFilterSlotPairListList.add(new ArrayList<>(filterSlotPairList));
                    // 对每个slot做排列组合
                    for (Slot slot: matchedSlotList) {
                        List<Pair<MaskedSlotFilter, Slot>> newFilterSlotPairList = new ArrayList<>(filterSlotPairList);
                        newFilterSlotPairList.add(new Pair<>(filter, slot));
                        newFilterSlotPairListList.add(newFilterSlotPairList);
                    }
                }
                filterSlotPairListList = newFilterSlotPairListList;
            }

            // 增加一个非捕获组的slot全部掩码的情形
            List<Pair<MaskedSlotFilter, Slot>> nonCapturingFilterSlotPairList = filter2nonCapturingSlotList.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream().map(slot -> new Pair<>(entry.getKey(), slot)))
                    .collect(Collectors.toList());
            if (nonCapturingFilterSlotPairList.size() <= 15) {
                // 2^16 = 65536，此时 CollectionUtils.getAllCombinationList 会计算耗时太长，近乎死循环
                List<List<Pair<MaskedSlotFilter, Slot>>> allCandidatePairListList = CollectionUtils.getAllCombinationList(nonCapturingFilterSlotPairList, x -> x);

                List<List<Pair<MaskedSlotFilter, Slot>>> filterSlotPairListToAdd = new ArrayList<>();
//            List<Slot> allNonCapturingSlotList = filter2nonCapturingSlotList.values().stream().flatMap(list -> list.stream()).collect(Collectors.toList());
                for (List<Pair<MaskedSlotFilter, Slot>> filterSlotPairList : filterSlotPairListList) {
                    for (List<Pair<MaskedSlotFilter, Slot>> candidatePairList : allCandidatePairListList) {
                        List<Slot> tmpSlotList = filterSlotPairList.stream()
                                .map(x -> x.getValue()).collect(Collectors.toList());
                        List<Pair<MaskedSlotFilter, Slot>> newFilterSlotPairList = new ArrayList<>(filterSlotPairList);
                        for (Pair<MaskedSlotFilter, Slot> candidatePair : candidatePairList) {
                            if (!tmpSlotList.contains(candidatePair.getValue())) {
                                newFilterSlotPairList.add(candidatePair);
                            }
                        }
                        filterSlotPairListToAdd.add(newFilterSlotPairList);
                    }
                }
//            Set<String> pairListKeySet = new HashSet<>(filterSlotPairListToAdd.size());
//            List<List<Pair<MaskedSlotFilter, Slot>>> derepeatedListList = new ArrayList<>();
//            for (List<Pair<MaskedSlotFilter, Slot>> filterSlotPairList: filterSlotPairListToAdd) {
//                filterSlotPairList = filterSlotPairList.stream().distinct()
//                        .sorted(Comparator.comparingInt(o -> o.getValue().getStartIndex()))
//                        .collect(Collectors.toList());
//                String pairListKey = filterSlotPairList.stream().map(pair -> pair.toString()).collect(Collectors.joining("\t"));
//                if (!pairListKeySet.contains(pairListKey)) {
//                    pairListKeySet.add(pairListKey);
//                    derepeatedListList.add(filterSlotPairList);
//                }
//            }
                if (filterSlotPairListToAdd.size() < MAX_SLOT_COUNT) {
                    filterSlotPairListList = filterSlotPairListToAdd;
                } else {
                    filterSlotPairListList.add(nonCapturingFilterSlotPairList);
                }
            } else {
                filterSlotPairListList.add(nonCapturingFilterSlotPairList);
            }

            // 负向断言的slot强制要求掩码
            for (List<Pair<MaskedSlotFilter, Slot>> filterSlotPairList: filterSlotPairListList) {
                List<Slot> tmpSlotList = filterSlotPairList.stream()
                        .map(x -> x.getValue()).collect(Collectors.toList());
                for (Map.Entry<MaskedSlotFilter, List<Slot>> entry: filter2assertionSlotList.entrySet()) {
                    MaskedSlotFilter filter = entry.getKey();
                    List<Slot> assertionSlotList = entry.getValue();
                    for (Slot assertionSlot: assertionSlotList) {
                        if (!tmpSlotList.contains(assertionSlot)) {
                            filterSlotPairList.add(new Pair<>(filter, assertionSlot));
                        }
                    }
                }
            }

            // 对每个情况进行slot识别
            for (List<Pair<MaskedSlotFilter, Slot>> filterSlotPairList: filterSlotPairListList) {
                // 同一个位置可能有多个不同的slot，或是同一个slot对应了多个不同的filter（包括出现在或中，但不能出现在前向断言、后向断言中）
                // 将相同位置的slot合并，以slot的start、end为slotKey
                // 对于一个filter对应多个slot的情况（非捕获组的重复多次），这些slot都对应同一个maskText
                Map<String, List<MaskedSlotFilter>> slotKey2filterList = new HashMap<>();
                Map<String, List<Slot>> slotKey2SlotList = new HashMap<>();
                Map<MaskedSlotFilter, List<Slot>> filter2SlotList = new HashMap<>();
                Map<String, Pair<Integer, Integer>> slotKey2IndexPair = new HashMap<>();
                for (Pair<MaskedSlotFilter, Slot> filterSlotPair : filterSlotPairList) {
                    MaskedSlotFilter filter = filterSlotPair.getKey();
                    Slot slot = filterSlotPair.getValue();
                    String slotKey = slot.getStartIndex() + "_" + slot.getEndIndex();
                    Pair<Integer, Integer> integerPair = new Pair<>(slot.getStartIndex(), slot.getEndIndex());

                    slotKey2filterList.putIfAbsent(slotKey, new ArrayList<>());
                    slotKey2filterList.get(slotKey).add(filter);

                    slotKey2SlotList.putIfAbsent(slotKey, new ArrayList<>());
                    slotKey2SlotList.get(slotKey).add(slot);

                    slotKey2IndexPair.put(slotKey, integerPair);

                    filter2SlotList.putIfAbsent(filter, new ArrayList<>());
                    filter2SlotList.get(filter).add(slot);
                }

                // 获取 maskedQuery
                String maskedQuery = query;
                // index2offset 中的 index：maskedQuery中maskedSlot部分的end位置，offset：maskedQuery中某个位置相对于rawQuery中该位置的偏移量（位置比原来向后挪了了多少），注意偏移量可能为负，表示向前挪（mask后的长度小于原始长度）
                Map<Integer, Integer> index2offset = new HashMap<>();
                List<String> slotKeyList = slotKey2filterList.keySet().stream()
                        .sorted(Comparator.comparing(x -> slotKey2IndexPair.get(x).getKey())
                                .thenComparing(x -> slotKey2IndexPair.get(x).getValue()))
                        .collect(Collectors.toList());
                int totalOffset = 0;
                boolean isSkip = false;
                Map<MaskedSlotFilter, String> filter2MaskText = new HashMap<>();
                int maskIndex = 0;
                for (String slotKey: slotKeyList) {
                    List<MaskedSlotFilter> filterList = slotKey2filterList.get(slotKey);
                    Slot slot = slotKey2SlotList.get(slotKey).get(0);
                    String rawValue = slot.getRawValue();
                    int start = slot.getStartIndex() + totalOffset;
                    int end = slot.getEndIndex() + totalOffset;
                    if (maskedQuery.substring(start, end).equals(rawValue)) {
                        maskIndex += 1;
                        String maskedText = getMaskedText(maskIndex);

                        List<MaskedSlotFilter> filterWithMultiSlotList = new ArrayList<>();
                        for (MaskedSlotFilter filter: filterList) {
                            if (filter2SlotList.get(filter).size() > 1) {
                                filterWithMultiSlotList.add(filter);
                            }
                        }
                        if (filterWithMultiSlotList.size() == 1) {
                            MaskedSlotFilter filter = filterWithMultiSlotList.get(0);
                            if (filter2MaskText.containsKey(filter)) {
                                maskedText = filter2MaskText.get(filter);
                            }
                        }
                        for (MaskedSlotFilter filter: filterList) {
                            filter2MaskText.put(filter, maskedText);
                        }
                        maskedQuery = maskedQuery.substring(0, start) + maskedText + maskedQuery.substring(end);
                        int offset = maskedText.length() - rawValue.length();
                        totalOffset += offset;
                        index2offset.put(end + offset, totalOffset);
                    } else {
                        isSkip = true;
                    }
                }

                if (isSkip) {
                    continue;
                }

//                List<MaskedSlotFilter> filterList = maskedSlotFilterList.stream()
                List<MaskedSlotFilter> filterList = Stream.of(maskedSlotFilterList.stream(), assertionMaskedSlotFilterList.stream(), nonCapturingMaskedSlotFilterList.stream()).flatMap(x -> x)
                        .sorted(Comparator.comparing(x -> x.getStartIndex()))
                        .collect(Collectors.toList());
                String tmpRule = strRule;
                int offset = 0;
                for (MaskedSlotFilter filter: filterList) {
                    int start = filter.getStartIndex() + offset;
                    int end = filter.getEndIndex() + offset;
                    String maskedText;
                    if (filter2MaskText.containsKey(filter)) {
                        maskedText = filter2MaskText.get(filter);
                    } else {
                        maskIndex += 1;
                        maskedText = getMaskedText(maskIndex);
                        filter2MaskText.put(filter, maskedText);
                    }
                    tmpRule = tmpRule.substring(0, start) + maskedText + tmpRule.substring(end);
                    offset += maskedText.length() - (end - start);
                }

                Matcher matcher = Pattern.compile(tmpRule, this.regexMode.getFlag()).matcher(maskedQuery); // compile 方法不传入flag参数，不允许跨行匹配
                while (matcher.find()) {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    if (matcher.groupCount() == slotNameList.size()) {
                        for (int i = 1; i <= matcher.groupCount(); i++) {
                            String slotName = slotNameList.get(i - 1);
                            String normedValue = normedValueList.get(i - 1);
                            int start = matcher.start(i);
                            int end = matcher.end(i);
                            boolean isStartOffsetChanged = false;
                            boolean isEndOffsetChanged = false;
                            int startOffset = 0;
                            int endOffset = 0;
                            for (Map.Entry<Integer, Integer> entry: index2offset.entrySet()) {
                                int tmpOffset = entry.getValue();
                                if (entry.getKey() <= start && (tmpOffset > startOffset || !isStartOffsetChanged)) {
                                    startOffset = tmpOffset;
                                    isStartOffsetChanged = true;
                                }
                                if (entry.getKey() <= end && (tmpOffset > endOffset|| !isEndOffsetChanged)) {
                                    endOffset = tmpOffset;
                                    isEndOffsetChanged = true;
                                }
                            }
                            int originalStart = start - startOffset;
                            int originalEnd = end - endOffset;
                            String rawValue = query.substring(originalStart, originalEnd);
                            if (normedValue == null) {
                                normedValue = rawValue;
                            }
//                            System.out.println(maskedQuery.substring(start, end) + "\t" + rawValue);
                            String[] curSlotNameArray = slotName.split("\\|", 0);
                            boolean retained = curSlotNameArray.length > 1;
                            for (String curSlotName: curSlotNameArray) {
                                Slot slot = new Slot(curSlotName, rawValue, normedValue, originalStart, originalEnd, score, retained, source, source + "_" + uuid);
                                slotList.add(slot);
                            }
                        }
                    }
                }
            }
        }
        return slotList;
    }

    public static void main(String[] args) {
//        System.out.println(getMaskedText(19));

        List<Pair<MaskedSlotFilter, Slot>> listT = IntStream.rangeClosed(1, 15).boxed()
                .map(idx -> new Pair<>(new MaskedSlotFilter(idx, idx, ImmutableMap.of("slotName", "1", "rawValue", "2", "normedValue", "3")), new Slot("slotName", "rawValue", "normedValue", idx, idx, 1, false,"37d0c845d9254951b77948ea5f2e7df7", "37d0c845d9254951b77948ea5f2e7df7")))
                        .collect(Collectors.toList());
        System.out.println(CollectionUtils.getAllCombinationList(listT, x -> x));
    }
}
