package org.example.ruledetect.slotrule;

import com.google.common.collect.Lists;
import org.example.ruledetect.bean.Slot;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SlotMergeTest {


    @Test
    public void regressionTest() {
        List<Slot> slotList;

        // score相同时取最长的
        slotList = Lists.newArrayList(
                new Slot("she", "she", "she", 1, 3, 1, false,"source", "evidence"),
                new Slot("he", "he", "he", 2, 3, 1, false,"source", "evidence"),
                new Slot("hers", "hers", "hers", 2, 5, 1, false,"source", "evidence")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 1
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("hers")));


        // 有重叠score相同时取最长的
        slotList = Lists.newArrayList(
                new Slot("she", "she", "she", 1, 3, 1, false,"source", "evidence"),
                new Slot("he", "he", "he", 2, 3, 1, false,"source", "evidence")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 1
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("she")));


        // 有重叠score相同时取最长的
        slotList = Lists.newArrayList(
                new Slot("she", "she", "she", 1, 3, 1, false,"source", "evidence"),
                new Slot("he", "he", "he", 1, 2, 1, false,"source", "evidence")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 1
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("she")));


        // 没有重叠时都保留
        slotList = Lists.newArrayList(
                new Slot("she", "she", "she", 1, 3, 1, false,"source", "evidence"),
                new Slot("he", "he", "he", 3, 5, 1, false,"source", "evidence")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 2
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("she"))
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("he")));


        // 有重叠score相同时取最长的
        slotList = Lists.newArrayList(
                new Slot("abc", "abc", "abc", 1, 4, 1, false,"source", "evidence"),
                new Slot("bcde", "bcde", "bcde", 2, 6, 1, false,"source", "evidence")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 1
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("bcde")));


        // 有重叠score相同时取最长的
        slotList = Lists.newArrayList(
                new Slot("cde", "cde", "cde", 3, 6, 1, false,"source", "evidence"),
                new Slot("abcd", "abcd", "abcd", 1, 5, 1, false,"source", "evidence")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 1
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abcd")));


        // 有重叠score不同取score大的
        slotList = Lists.newArrayList(
                new Slot("abc", "abc", "abc", 1, 4, 2, false,"source", "evidence"),
                new Slot("def", "def", "def", 4, 7, 1, false,"source", "evidence"),
                new Slot("bcde", "bcde", "bcde", 2, 6, 1, false,"source", "evidence")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 2
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abc"))
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("def")));


        // 有重叠score不同取score大的
        slotList = Lists.newArrayList(
                new Slot("abc", "abc", "abc", 1, 4, 2, false,"source", "evidencr"),
                new Slot("bcd", "bcd", "bcd", 2, 5, 1, false,"source", "evidence"),
                new Slot("ef", "ef", "ef", 8, 9, 1, false,"source", "evidence"),
                new Slot("efg", "efg", "efg", 8, 10, 2, false,"source", "evidence")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 2
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("bcd"))
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("efg")));


        // retained有重叠取最长的
        slotList = Lists.newArrayList(
                new Slot("abc1", "abc", "abc", 1, 4, 2, true,"source", "evidencr"),
                new Slot("abc2", "abc", "abc", 1, 4, 2, true, "source", "evidencr"),
                new Slot("abcd", "abcd", "abcd", 1, 5, 2, true, "source", "evidencr")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 1
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abcd")));


        // retained和非retained有重叠取最长的
        slotList = Lists.newArrayList(
                new Slot("abc1", "abc", "abc", 1, 4, 2, true,"source", "evidencr"),
                new Slot("abc2", "abc", "abc", 1, 4, 2, true, "source", "evidencr"),
                new Slot("abcd", "abcd", "abcd", 1, 5, 2, false, "source", "evidencr")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 1
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abcd")));


        // retained和非retained有重叠取最长的，同时验证retained是否有效
        slotList = Lists.newArrayList(
                new Slot("abc1", "abc", "abc", 1, 4, 2, true,"source", "evidencr"),
                new Slot("abc2", "abc", "abc", 1, 4, 2, true, "source", "evidencr"),
                new Slot("ab", "ab", "ab", 1, 3, 2, false, "source", "evidencr")
        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 2
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abc1"))
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abc2")));


        // retained和非retained有重叠取最长的，同时验证retained是否有效
        slotList = Lists.newArrayList(
                new Slot("abc1", "abc", "abc", 1, 4, 2, true,"source", "evidencr"),
                new Slot("ab", "ab", "ab", 1, 3, 2, false, "source", "evidencr"),
                new Slot("abc2", "abc", "abc", 1, 4, 2, true, "source", "evidencr")

        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 2
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abc1"))
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abc2")));


        // retained和非retained有重叠取最长的，同时验证retained是否有效
        slotList = Lists.newArrayList(
                new Slot("ab", "ab", "ab", 1, 3, 2, false, "source", "evidencr"),
                new Slot("abc1", "abc", "abc", 1, 4, 2, true,"source", "evidencr"),
                new Slot("ab", "ab", "ab", 1, 3, 2, false, "source", "evidencr"),
                new Slot("abc2", "abc", "abc", 1, 4, 2, true, "source", "evidencr")

        );
        slotList = SlotMerge.mergeSlots(slotList);
        Assert.assertTrue(slotList.size() == 2
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abc1"))
                && slotList.stream().anyMatch(slot -> slot.getSlotName().equals("abc2")));

    }

}