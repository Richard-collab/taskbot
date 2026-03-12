package org.example.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StringSplitterByByteLength {

    public static List<String> splitLineByByteLength(String msg, int len) {
        String[] lineArray = msg.split("\n");
        String text = "";
        List<String> textList = new ArrayList<>();
        for (String line: lineArray) {
//            String newText = text + "\n" + line;
            String newText;
            if (text.isEmpty()) {
                newText = line;
            } else {
                newText = text + "\n" + line;
            }
            if (newText.getBytes(StandardCharsets.UTF_8).length < len) {
                text = newText;
            } else {
                if (!text.isEmpty()) {
                    textList.add(text);
                }
                List<String> tmpList = splitByByteLength(line, len);
                for (int i = 0; i < tmpList.size() - 1; i++) {
                    textList.add(tmpList.get(i));
                }
                text = tmpList.get(tmpList.size() - 1);
            }
        }
        if (!text.isEmpty()) {
            textList.add(text);
        }
        return textList;
    }

    public static List<String> splitByByteLength(String msg, int len) {
        List<String> result = new ArrayList<>();
        if (msg == null || msg.isEmpty() || len <= 0) {
            return result;
        }

        int index = 0;
        while (index < msg.length()) {
            // 获取当前字符的Unicode代码点
            int codePoint = msg.codePointAt(index);
            String ch = new String(new int[]{codePoint}, 0, 1);
            int charByteLen = ch.getBytes(StandardCharsets.UTF_8).length;

            // 处理单个字符超过限制的情况
            if (charByteLen > len) {
                // 将字符作为独立元素加入列表（即使超过长度限制）
                result.add(ch);
                index += Character.charCount(codePoint);
                continue;
            }

            // 构建当前分段的字符串
            StringBuilder current = new StringBuilder();
            int currentByteLen = 0;

            // 向当前分段添加字符，直到达到长度限制
            while (index < msg.length()) {
                codePoint = msg.codePointAt(index);
                ch = new String(new int[]{codePoint}, 0, 1);
                charByteLen = ch.getBytes(StandardCharsets.UTF_8).length;

                // 检查添加后是否超过长度限制
                if (currentByteLen + charByteLen > len) {
                    break;
                }

                // 添加字符到当前分段
                current.append(ch);
                currentByteLen += charByteLen;
                index += Character.charCount(codePoint);
            }

            result.add(current.toString());
        }

        return result;
    }

    public static void main(String[] args) {
        String msg = "你好\nabc世界！\n你说什么？啥子？";
        int len = 15;
        List<String> parts = splitLineByByteLength(msg, len);
        System.out.println("分段结果（每段最多" + len + "字节）:");
        for (String part : parts) {
            System.out.println(part + " [字节长度: " + part.getBytes(StandardCharsets.UTF_8).length + "]");
        }
    }
}
