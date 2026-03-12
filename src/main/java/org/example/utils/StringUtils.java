package org.example.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {

    private static final Pattern PATTERN_IP = Pattern.compile("^((?:(?:\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(?:\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5]))$");
    private static final Pattern PATTERN_URL = Pattern.compile("^https?://([-A-Za-z0-9]+(\\.[-A-Za-z])+|(?:(?:\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(?:\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5]))(:[0-9]{1,5})?[-A-Za-z0-9+&@#/%=~_|?.]*$");
    private static final Pattern PATTERN_ARABIC_HOUR_MINUTE = Pattern.compile("^([0-1][0-9]|2[0-4]):[0-5][0-9]$");

    private static final Map<Character, Character> PAIRED_PUNCTUATIONS = new HashMap<Character, Character>() {{
        put('「', '」');
        put('『', '』');
        put('《', '》');
        put('〈', '〉');
        put('（', '）');
        put('[', ']');
        put('(', ')');
        put('{', '}');
        put('"', '"');
        put('“', '”');
        put('‘', '’');
        put('【', '】');
        put('〖', '〗');
    }};

    public static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

    /**
     全角转半角的函数(DBC case)
     全角空格为12288，半角空格为32
     其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * @param input 任意字符串
     * @return 半角字符串
     *
     */
    public static String toDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                //全角空格为12288，半角空格为32
                c[i] = (char) 32;
            } else if (c[i] > 65280 && c[i] < 65375) {
                //其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

//    /**
//     半角转全角的方法(SBC case)
//     全角空格为12288，半角空格为32
//     其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
//     * @param input 任意字符串
//     * @return 半角字符串
//     *
//     */
//    public static String toSBC(String input)
//    {
//        char[] c=input.toCharArray();
//        for (int i = 0; i < c.length; i++)
//        {
//            if (c[i]==32)
//            {
//                c[i]=(char)12288;
//                continue;
//            }
//            if (c[i]<127)
//                c[i]=(char)(c[i]+65248);
//        }
//        return new String(c);
//    }

    /**
     半角转全角的方法(SBC case)
     全角空格为12288，半角空格为32
     其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * @param input 任意字符串
     * @return 半角字符串
     *
     */
    public static String toSBC(String input)
    {
        char[] c=input.toCharArray();
        for (int i = 0; i < c.length; i++)
        {
            // 遇到 !(),:;? 则半角转全角
            if (c[i]==33 || c[i]==40 || c[i]==41 || c[i]==44|| c[i]==58 || c[i]==59 || c[i]==63) {
                c[i] = (char) (c[i] + 65248);
            } else if (c[i] == 12288) { // 全角空格转半角空格
                //全角空格为12288，半角空格为32
                c[i] = (char) 32;
            } else if (c[i] > 65280 && c[i] < 65375) { // 全角字符转半角字符
                //其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * CJK类字符判断（包括中文字符也在此列），参考：https://en.wikipedia.org/wiki/CJK_Unified_Ideographs_(Unicode_block)
     *
     * @param c 待判断的字符
     * @return 是否是CJK类字符
     */
    public static boolean isCjkChar(char c) {
        return (0x4E00 <= c && c <= 0x9FFF) || (0x3400 <= c && c <= 0x4DBF)
                || (0x20000 <= c && c <= 0x2A6DF) || (0x2A700 <= c && c <= 0x2B73F)
                || (0x2B740 <= c && c <= 0x2B81F) || (0x2B820 <= c && c <= 0x2CEAF)
                || (0xF900 <= c && c <= 0xFAFF) || (0x2F800 <= c && c <= 0x2FA1F);
    }

    public static boolean isChinesePunctuation(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
//                || ub == Character.UnicodeBlock.
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
                || ub == Character.UnicodeBlock.VERTICAL_FORMS) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEnglishPunctuation(char c) {
        return (33 <= c && c <= 47 ) || (58 <= c && c <= 64) || (91 <= c && c <= 96) || (123 <= c && c <= 126);
    }

    public static boolean isPunctuation(char c) {
        return isEnglishPunctuation(c) || isChinesePunctuation(c);
    }
    public static String removePunctuation(String input) {
        char[] charArray = input.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c: charArray) {
            if (c == '.' || !StringUtils.isPunctuation(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isIp(String input) {
        return input != null && PATTERN_IP.matcher(input).matches();
    }

    public static boolean isUrl(String input) {
        return input != null && PATTERN_URL.matcher(input).matches();
    }

    public static boolean isArabicHourMinute(String input) {
        return input != null && PATTERN_ARABIC_HOUR_MINUTE.matcher(input).matches();
    }

    public static List<String> splitChar(String input) {
        return Arrays.stream(input.trim().split("", 0)).collect(Collectors.toList());
    }

    /**
     * 智能分割函数：保护成对标点内的内容
     *
     * @param text 待分割文本
     * @param delimiters 分割符字符串
     * @return
     */
    public static List<String> smartSplitChar(String text, Set<Character> delimiters, boolean isRetainTailPunc) {
        List<String> fragments = new ArrayList<>();
        StringBuilder currentFragment = new StringBuilder();
        Stack<Character> stack = new Stack<>(); // 用于跟踪成对标点的栈

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            // 检查是否是成对标点的左括号（如果是英文引号，不允许有嵌套）
            if (PAIRED_PUNCTUATIONS.containsKey(ch) &&
                    (stack.isEmpty() || ch != '"' || !stack.contains(ch))) {
                stack.push(ch);
                currentFragment.append(ch);
            }
            // 检查是否是成对标点的右括号
            else if (PAIRED_PUNCTUATIONS.containsValue(ch)) {
                // 检查栈顶是否匹配
                if (!stack.isEmpty()) {
                    char lastLeft = stack.peek();
                    if (PAIRED_PUNCTUATIONS.get(lastLeft) != null &&
                            PAIRED_PUNCTUATIONS.get(lastLeft) == ch) {
                        stack.pop();
                    }
                }
                currentFragment.append(ch);
            }
            // 检查是否是分隔符
            else if (delimiters.contains(ch) && stack.isEmpty()) {
                // 如果不在成对标点内部，并且当前片段不为空，则添加到结果中
                if (currentFragment.toString().trim().length() > 0) {
                    if (isRetainTailPunc) {
                        currentFragment.append(ch);
                    }
                    fragments.add(currentFragment.toString().trim());
                    currentFragment = new StringBuilder();
                }
            } else {
                currentFragment.append(ch);
            }
        }

        // 添加最后一个片段
        if (currentFragment.toString().trim().length() > 0) {
            fragments.add(currentFragment.toString().trim());
        }

        return fragments;
    }

    /**
     * 智能分割函数：保护成对标点内的内容
     *
     * @param text 待分割文本
     * @param delimiterString 分割符字符串
     * @return
     */
    public static List<String> smartSplitChar(String text, String delimiterString, boolean isRetainTailPunc) {
        Set<Character> delimiters = new HashSet<>();
        for (char ch : delimiterString.toCharArray()) {
            delimiters.add(ch);
        }
        return smartSplitChar(text, delimiters, isRetainTailPunc);
    }

    /**
     * 给定字符串strA和strB，判断strB是否是由strA通过增加字符得来的（可以增加0个或任意个字符，每个字符的增加位置可以是开头、中间、结尾）
     *
     * @param strA
     * @param strB
     * @return
     */
    public static boolean isSubsequence(String strA, String strB) {
        int index = -1;

        for (char c : strA.toCharArray()) {
            index = org.apache.commons.lang3.StringUtils.indexOf(strB, c, index + 1);
            if (index == -1) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
//        System.out.println(toDBC("，。？！“”"));
//        System.out.println(splitChar(" 123 "));
//        System.out.println(isIp("192.168.0.253"));
        System.out.println(isUrl("http://192.168.231.112:4006"));
        System.out.println(isUrl("http://jxt.goutbound.status.jinxintian.cn/goutbound-call-status-push/statusPush/outbound/baize/task/66201?a=2"));
        System.out.println(smartSplitChar("他说：\"他不在。难道不是吗？\"随手拿了一本书：“这本《在吗？在的》不错，你觉得呢？”她笑而不语。就这么看着他。", ",，。!！?？", true));
    }
}
