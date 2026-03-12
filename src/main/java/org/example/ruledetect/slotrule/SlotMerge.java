package org.example.ruledetect.slotrule;

import com.google.common.collect.Lists;
import org.example.ruledetect.bean.Slot;
import org.example.utils.NumUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SlotMerge {

    /**
     * 合并slot列表，slot有重叠的，优先保留score较大的slot，score相同则优先保留长度较长的slot
     *
     * @param slotList 待合并的slot列表
     * @return 合并后的slot列表
     */
    public static List<Slot> mergeSlots(List<Slot> slotList) {
        if (slotList == null || slotList.size() <= 1) {
            return slotList;
        } else {
            slotList = slotList.stream()
                    .sorted(Comparator.comparing(Slot::getStartIndex))
                    .collect(Collectors.toList());
            // 如果slotA完全包含slotB，那只保留slotA
            final List<Slot> filteredSlotList = new ArrayList<>();
            Slot focusSlot = slotList.get(0);
            for (int i = 1; i < slotList.size(); i++) {
                Slot curSlot = slotList.get(i);
                if (judgeContain(focusSlot, curSlot) || judgeContain(curSlot, focusSlot)) {
                    if (judgeContain(curSlot, focusSlot)) {
                        focusSlot = curSlot;
                    }
                } else {
                    filteredSlotList.add(focusSlot);
                    focusSlot = curSlot;
                }
            }
            filteredSlotList.add(focusSlot);

            slotList = filteredSlotList;

            final List<Slot> mergedSlotList = new ArrayList<>();
            // 同一个slot模板包含的slot槽位越多，优先级越高
            Map<Slot, Integer> slot2levelScore = new HashMap<>();
            Map<String, List<Slot>> source2slotList = slotList.stream()
                    .collect(Collectors.groupingBy(x -> x.getEvidence(), Collectors.mapping(y -> y, Collectors.toList())));
            Map<Integer, List<List<Slot>>> count2slotListList = source2slotList.values().stream()
                    .collect(Collectors.groupingBy(x -> x.size(), Collectors.mapping(y -> y, Collectors.toList())));
            count2slotListList.entrySet().stream()
                    .sorted(Map.Entry.<Integer, List<List<Slot>>>comparingByKey().reversed())
                    .forEach(entry -> {
                        int count = entry.getKey();
                        List<List<Slot>> slotListList = entry.getValue();
                        for (List<Slot> tmpSlotList: slotListList) {
                            for (Slot slot: tmpSlotList) {
                                slot2levelScore.put(slot, count);
                            }
                        }
                    });

            Slot targetSlot = slotList.get(0);
            for (int i = 1; i < slotList.size(); i++) {
                Slot curSlot = slotList.get(i);
                if (judgeOverlap(targetSlot, curSlot)
                        && !(judgeEqual(targetSlot, curSlot) && targetSlot.isRetained() && curSlot.isRetained())) {
                    int targetLevelScore = slot2levelScore.get(targetSlot);
                    int curLevelScore = slot2levelScore.get(curSlot);
                    float targetScore = targetSlot.getScore();
                    float curScore = curSlot.getScore();
                    if (curLevelScore == targetLevelScore && NumUtils.equals(targetScore, curScore)) { // slot得分相同
                        if (curSlot.getLength() > targetSlot.getLength()) {
                            targetSlot = curSlot;
                        } else if (curSlot.getLength() == targetSlot.getLength()) {
                            if (curSlot.isRetained() && !targetSlot.isRetained()) {
                                targetSlot = curSlot;
                            }
                        }
                    } else { // slot得分不同
                        if (curLevelScore > targetLevelScore ||
                                (curLevelScore == targetLevelScore && curSlot.getScore() > targetSlot.getScore())) {
                            targetSlot = curSlot;
                        }
                    }
                } else {
                    mergedSlotList.add(targetSlot);
                    targetSlot = curSlot;
                }
            }
            mergedSlotList.add(targetSlot);
            return mergedSlotList;
        }
    }

    private static boolean judgeContain(Slot slot1, Slot slot2) {
        // 判定是否slot1真包含slot2
        return (slot1.getStartIndex() < slot2.getStartIndex() && slot1.getEndIndex() >= slot2.getEndIndex())
                || (slot1.getStartIndex() <= slot2.getStartIndex() && slot1.getEndIndex() > slot2.getEndIndex());
    }
    private static boolean judgeOverlap(Slot slot1, Slot slot2) {
        // 不能等于，因为endIndex是slot中最后一个字的index加1
        return Math.max(slot1.getStartIndex(), slot2.getStartIndex()) < Math.min(slot1.getEndIndex(), slot2.getEndIndex());
    }

    private static boolean judgeEqual(Slot slot1, Slot slot2) {
        return  slot1.getStartIndex() == slot2.getStartIndex() && slot1.getEndIndex() == slot2.getEndIndex();
    }

    public static void main(String[] args) {
        List<Slot> slotList;

        slotList = Lists.newArrayList(
                new Slot("she", "she", "she", 1, 3, 1, false,"source", "evidence"),
                new Slot("he", "he", "he", 2, 3, 1, false,"source", "evidence"),
                new Slot("hers", "hers", "hers", 2, 5, 1, false,"source", "evidence")
        );
        System.out.println(mergeSlots(slotList));
    }
}
