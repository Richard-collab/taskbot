package org.example;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUU {

    private static List<List<Integer>> groupSlots(List<Integer> slots, String query) {
        // 生成各行的起始位置列表
        List<Integer> lineStarts = new ArrayList<>();
        lineStarts.add(0);
        int idx = 0;
        for (char c: query.toCharArray()) {
            if (c == '\n') {
                lineStarts.add(idx + 1);
            }
            idx += 1;
        }

        // 转换为数组以便二分查找
        int[] lineStartsArray = lineStarts.stream().mapToInt(Integer::intValue).toArray();

        // 初始化分组列表
        List<List<Integer>> groups = new ArrayList<>();
        for (int i = 0; i < lineStarts.size(); i++) {
            groups.add(new ArrayList<>());
        }

        // 分配每个slot到对应的组
        for (int slot : slots) {
            int start = slot;
            // 使用二分查找确定行索引
//            int lineIndex = findLineIndex(lineStartsArray, start);
            int lineIndex = Arrays.binarySearch(lineStartsArray, start);
            if (lineIndex < 0) {
                lineIndex = -lineIndex - 2;
            }
            if (lineIndex >= 0 && lineIndex < groups.size()) {
                groups.get(lineIndex).add(slot);
            }
        }

        return groups;
    }

    public static void main(String[] args) {
        List<Integer> slots= Lists.newArrayList(1,2,4,5,7,8);
        String query = "012\n45\n78\n";
        List<List<Integer>> result = groupSlots(slots, query);
        System.out.println(result);

    }
}
