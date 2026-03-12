package org.example.utils;


import java.util.HashMap;
import java.util.Map;

public class ChineseArabicNumConverter {

    private static final Map<Character, Integer> digitMap = new HashMap<>();
    private static final Map<Character, Long> unitMap = new HashMap<>();

    static {
        // 数字映射
        digitMap.put('零', 0);
        digitMap.put('一', 1);
        digitMap.put('二', 2);
        digitMap.put('两', 2);
        digitMap.put('三', 3);
        digitMap.put('四', 4);
        digitMap.put('五', 5);
        digitMap.put('六', 6);
        digitMap.put('七', 7);
        digitMap.put('八', 8);
        digitMap.put('九', 9);

        // 单位映射
        unitMap.put('十', 10L);
        unitMap.put('百', 100L);
        unitMap.put('千', 1000L);
        unitMap.put('万', 10_000L);
        unitMap.put('亿', 100_000_000L);
    }

    public static long chineseToArabic(String input) {
        long currentBaseUnit = 1;
        long total = 0;
        int index = 0;
        int length = input.length();

        while (index < length) {
            char currentChar = input.charAt(index);

            // 处理阿拉伯数字部分（例如 "30万" 中的 30）
            if (Character.isDigit(currentChar)) {
                int start = index;
                while (index < length && Character.isDigit(input.charAt(index))) {
                    index++;
                }
                long num = Long.parseLong(input.substring(start, index));

                // 检查后续是否有中文单位（如 "万"）
                if (index < length && unitMap.containsKey(input.charAt(index))) {
                    char unitchar = input.charAt(index);
                    long unit = unitMap.get(unitchar);
                    total += num * unit;
                    if (index + 2 < length && Character.isDigit(input.charAt(index + 2))) {
                        currentBaseUnit = 1;
                    } else {
                        // 更新基准单位（例如千→百，百→十）
                        currentBaseUnit = unit / 10;
                    }
                    index++; // 跳过单位字符
                } else {
                    total += num * currentBaseUnit;
                }
            }
            // 处理中文数字部分（例如 "万五" 中的 "万" 和 "五"）
            else {
                long yi = 0;     // 亿级
                long wan = 0;    // 万级
                long temp = 0;    // 当前段（千、百、十）
                long current = 0; // 当前数字
                currentBaseUnit = 1; // 基准单位（用于省略单位的情况）

                while (index < length && !Character.isDigit(input.charAt(index))) {
                    char c = input.charAt(index);
                    if (digitMap.containsKey(c)) {
                        current = digitMap.get(c);
                        if (c == '零') currentBaseUnit = 1; // 零重置基准单位
                    } else if (unitMap.containsKey(c)) {
                        long unit = unitMap.get(c);
                        if (unit <= 1000L) { // 十、百、千
                            if (current == 0) current = 1;
                            temp += current * unit;
                            current = 0;
                            // 更新基准单位（例如千→百，百→十）
                            currentBaseUnit = unit / 10;
                        } else if (unit == 10_000L) { // 万
                            wan += (temp + current) * unit;
                            temp = 0;
                            current = 0;
                            currentBaseUnit = 1000; // 万之后，基准单位设为千
                        } else if (unit == 100_000_000L) { // 亿
                            yi += (wan + temp + current) * unit;
                            wan = 0;
                            temp = 0;
                            current = 0;
                            currentBaseUnit = 10_000_000; // 亿之后，基准单位设为千万
                        }
                    } else {
                        throw new IllegalArgumentException("无效字符: " + c);
                    }
                    index++;
                }
                // 累加中文部分的结果（例如 "五" → 5 * 基准单位）
                total += yi + wan + temp + current * currentBaseUnit;
            }
        }
        return total;
    }

    public static void main(String[] args) {
        System.out.println(chineseToArabic("1千5"));
        System.out.println(chineseToArabic("1千500"));
        System.out.println(chineseToArabic("一千零一十万"));
        System.out.println(chineseToArabic("1010万"));
        System.out.println(chineseToArabic("九千八百七十六万五千四百三十二"));
        System.out.println(chineseToArabic("两百一十三万一千二百三十一"));
        System.out.println(chineseToArabic("三千五百万零六十"));
        System.out.println(chineseToArabic("1万5千"));
        System.out.println(chineseToArabic("一万五"));
        System.out.println(chineseToArabic("1万5"));
        System.out.println(chineseToArabic("一千五"));
        System.out.println(chineseToArabic("1千5"));
        System.out.println(chineseToArabic("15"));
    }
}