package org.example.utils;

import java.util.ArrayList;
import java.util.List;

public class CapturingGroupExtractor {

    List<int[]> rangeList;

    public CapturingGroupExtractor(String regex) {
        this.rangeList = getCapturingGroupList(regex);
    }

    public boolean isPositionInCapturingGroup(int position) {
        for (int[] range : this.rangeList) {
            if (position >= range[0] && position <= range[1]) {
                return true;
            }
        }
        return false;
    }

    public static List<int[]> getCapturingGroupList(String regex) {
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
            if (c == '(' && i + 1 < regex.length()) {
                int lookaheadType = checkCapturingGroup(regex, i);
                if (lookaheadType == 0) {
                    int end = findMatchingParenthesis(regex, i);
                    if (end != -1) {
                        ranges.add(new int[]{i, end});
//                        i = end; // 跳过已处理的部分
                    }
                }
            }
        }
        return ranges;
    }

    private static int checkCapturingGroup(String regex, int start) {
        if (start + 2 >= regex.length()) return -1;
        char nextChar = regex.charAt(start + 2);
        if (regex.charAt(start + 1) == '?') {
            if (nextChar == '!') {
                return 1; // 前向否定断言
            } else if (nextChar == '<' && start + 3 < regex.length() && regex.charAt(start + 3) == '!') {
                return 2; // 后向否定断言
            } else if (nextChar == '=') {
                return 3;// 前向肯定断言
            } else if (nextChar == '<' && start + 3 < regex.length() && regex.charAt(start + 3) == '=') {
                return 4; // 后向肯定断言
            } else if (nextChar == ':') {
                return 5; // 非捕获组
            }
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
        CapturingGroupExtractor extractor = new CapturingGroupExtractor("a(?:你好(?:11(12)235))b(?=12)(?!34)(?<=56)(?<!35)");
        System.out.println(extractor.isPositionInCapturingGroup(3));
    }
}
