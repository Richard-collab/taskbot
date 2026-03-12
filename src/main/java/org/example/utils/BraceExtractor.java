package org.example.utils;

import org.example.utils.StringUtils;
import org.example.utils.bean.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽取指定标记符号中间的内容
 */
public class BraceExtractor {

    public static List<Item> extract(String input, String left, String right) {
        List<Item> result = new ArrayList<>();
        if (StringUtils.isEmpty(input) || StringUtils.isEmpty(left) || StringUtils.isEmpty(right)) {
            return result;
        }
        int i = 0;
        while (i < input.length() - 1) {
            boolean leftFlag = true;
            for (int k = 0; k < left.length(); k++) {
                if (!(i + k < input.length() && input.charAt(i + k) == left.charAt(k))) {
                    leftFlag = false;
                    break;
                }
            }
            if (leftFlag) {
                int start = i + left.length();
                int end = -1;
                int j = start;
                while (j < input.length() - 1) {
                    boolean rightFlag = true;
                    for (int k = 0; k < right.length(); k++) {
                        if (!(j + k < input.length() && input.charAt(j + k) == right.charAt(k))) {
                            rightFlag = false;
                            break;
                        }
                    }
                    if (rightFlag) {
                        end = j;
                        break;
                    }
                    j++;
                }
                if (end != -1) {
                    String value = input.substring(start, end);
                    Item item = new Item(value, start, end);
                    result.add(item);
                    i = end + 2;
                } else {
                    i = start; // 跳过未闭合的起始标记
                }
            } else {
                i++;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String test1 = "abc{{content}}def{{another}}xyz";
        System.out.println(extract(test1, "{{", "}}")); // [content, another]

        String test2 = "no braces here";
        System.out.println(extract(test2,"{{", "}}")); // []

        String test3 = "{{unclosed";
        System.out.println(extract(test3, "{{", "}}")); // []

        String test4 = "{{a}}{{b}}";
        System.out.println(extract(test4,"{{", "}}")); // [a, b]
    }
}
