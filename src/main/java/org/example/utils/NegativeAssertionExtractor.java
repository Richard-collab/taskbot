package org.example.utils;

import java.util.ArrayList;
import java.util.List;

public class NegativeAssertionExtractor {

    List<int[]> assertionRangeList;

    public NegativeAssertionExtractor(String regex) {
        this.assertionRangeList = getNegativeLookaround(regex);
    }

    public boolean isPositionInNegativeLookaround(int position) {
        for (int[] range : this.assertionRangeList) {
            if (position >= range[0] && position <= range[1]) {
                return true;
            }
        }
        return false;
    }

    public static List<int[]> getNegativeLookaround(String regex) {
        List<int[]> assertionRanges = new ArrayList<>();

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
                int lookaheadType = checkNegativeLookaround(regex, i);
                if (lookaheadType != 0) {
                    int end = findMatchingParenthesis(regex, i);
                    if (end != -1) {
                        assertionRanges.add(new int[]{i, end});
                        i = end; // 跳过已处理的部分
                    }
                }
            }
        }
        return assertionRanges;
    }

    private static int checkNegativeLookaround(String regex, int start) {
        if (start + 2 >= regex.length()) return 0;
        char nextChar = regex.charAt(start + 2);
        if (nextChar == '!') {
            return 1; // 前向否定断言
        } else if (nextChar == '<' && start + 3 < regex.length() && regex.charAt(start + 3) == '!') {
            return 2; // 后向否定断言
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

}
