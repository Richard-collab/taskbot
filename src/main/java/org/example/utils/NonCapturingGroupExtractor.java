package org.example.utils;

import java.util.ArrayList;
import java.util.List;

public class NonCapturingGroupExtractor {

    List<int[]> rangeList;

    public NonCapturingGroupExtractor(String regex) {
        this.rangeList = getNonCapturingGroupList(regex);
    }

    public boolean isPositionInNonCapturingGroup(int position) {
        for (int[] range : this.rangeList) {
            if (position >= range[0] && position <= range[1]) {
                return true;
            }
        }
        return false;
    }

    public static List<int[]> getNonCapturingGroupList(String regex) {
        List<int[]> ranges = new ArrayList<>();

        boolean escaped = false;
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '(' && i + 1 < regex.length() && regex.charAt(i + 1) == '?') {
                int lookaheadType = checkNonCapturingGroup(regex, i);
                if (lookaheadType != 0) {
                    int end = findMatchingParenthesis(regex, i);
                    if (end != -1) {
                        ranges.add(new int[]{i, end});
                        i = end; // 跳过已处理的部分
                    }
                }
            }
        }
        return ranges;
    }

    private static int checkNonCapturingGroup(String regex, int start) {
        if (start + 2 >= regex.length()) return 0;
        char nextChar = regex.charAt(start + 2);
        if (nextChar == ':') {
            return 5;
        }
        return 0;
    }

    private static int findMatchingParenthesis(String regex, int start) {
        int level = 1;
        boolean escaped = false;
        for (int i = start + 1; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '(') {
                level++;
            } else if (c == ')') {
                level--;
                if (level == 0) {
                    return i;
                }
            }
        }
        return -1; // 未找到匹配的括号
    }

    public static void main(String[] args) {
        NonCapturingGroupExtractor extractor = new NonCapturingGroupExtractor("a(?:你好(?:1))b(?:)");
        System.out.println(extractor.isPositionInNonCapturingGroup(3));
    }
}
