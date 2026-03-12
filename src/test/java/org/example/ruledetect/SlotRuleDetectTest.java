package org.example.ruledetect;

import com.google.common.collect.Sets;
import junit.framework.TestCase;
import org.example.ruledetect.bean.SlotRuleType;
import org.example.ruledetect.bean.SlotRuleBean;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SlotRuleDetectTest extends TestCase {

    @Test
    public void testResource() {
        List<String> sourceList = SlotRuleDetect.getSlotRuleBeanList().stream()
                .map(slot -> slot.getSource())
                .collect(Collectors.toList());
        Set<String> sourceSet = new HashSet<>(sourceList);
        for (int i = 0; i < sourceList.size(); i++) {
            String source = sourceList.get(i);
            if (sourceSet.contains(source)) {
                sourceSet.remove(source);
                sourceList.remove(i);
                i--;
            }
        }
        assertEquals("slot rule 的 source 有重复：" + String.join("，",sourceList), 0, sourceList.size());
    }

    @Test
    public void testSlotNameCount() {
        List<SlotRuleBean> slotRuleBeanList = SlotRuleDetect.getSlotRuleBeanList();
        for (SlotRuleBean slotRuleBean: slotRuleBeanList) {
            String[] items = slotRuleBean.getSlotName().split(";", 0);
            for (String strRule: slotRuleBean.getStrRuleList()) {
                int regexSlotCount = 0;
                if (Sets.newHashSet(SlotRuleType.REGEX, SlotRuleType.SLOT_REGEX).contains(slotRuleBean.getRuleType())) {
                    regexSlotCount = RegexCaptureGroupCounter.countCaptureGroups(strRule);
                } else if (slotRuleBean.getRuleType() == SlotRuleType.WHITELIST) {
                    regexSlotCount = 1;
                }
                assertEquals("source：" + slotRuleBean.getSource() + "\n" + strRule + "\n" + "预期slot数目：" + items.length + "，实际slot数目：" + regexSlotCount, items.length, regexSlotCount);
            }
        }
    }

    public void testPredict() {
    }

    static class RegexCaptureGroupCounter {
        public static int countCaptureGroups(String regex) {
            int count = 0;
            boolean escaped = false;
            int i = 0;
            while (i < regex.length()) {
                char c = regex.charAt(i);
                if (escaped) {
                    escaped = false;
                    i++;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    i++;
                    continue;
                }
                if (c == '(') {
                    if (i + 1 < regex.length()) {
                        char nextC = regex.charAt(i + 1);
                        if (nextC == '?') {
                            int j = i + 2;
                            if (j >= regex.length()) {
                                i++;
                                continue;
                            }
                            char typeChar = regex.charAt(j);
                            switch (typeChar) {
                                case ':':
                                case '=':
                                case '!':
                                case '>':
                                    i = j + 1;
                                    continue;
                                case '#': {
                                    int closeParen = regex.indexOf(')', j + 1);
                                    if (closeParen != -1) {
                                        i = closeParen + 1;
                                    } else {
                                        i = regex.length();
                                    }
                                    continue;
                                }
                                case '<': {
                                    if (j + 1 < regex.length()) {
                                        char afterLT = regex.charAt(j + 1);
                                        if (afterLT == '=' || afterLT == '!') {
                                            i = j + 2;
                                        } else {
                                            count++;
                                            i = j + 1;
                                        }
                                    } else {
                                        i = j + 1;
                                    }
                                    continue;
                                }
                                default: {
                                    if (Character.isLetter(typeChar) || typeChar == '-') {
                                        int closeParen = regex.indexOf(')', j);
                                        if (closeParen != -1) {
                                            i = closeParen + 1;
                                        } else {
                                            i = regex.length();
                                        }
                                    } else {
                                        i = j + 1;
                                    }
                                    continue;
                                }
                            }
                        } else {
                            count++;
                        }
                    }
                    i++;
                } else {
                    i++;
                }
            }
            return count;
        }
    }
}

