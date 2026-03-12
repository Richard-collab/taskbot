package org.example.utils;

import junit.framework.TestCase;
import org.junit.Test;

public class ChineseArabicNumConverterTest extends TestCase {

    @Test
    public void testBasicNumbers() {
        assertEquals(0, ChineseArabicNumConverter.chineseToArabic("零"));
        assertEquals(1, ChineseArabicNumConverter.chineseToArabic("一"));
        assertEquals(10, ChineseArabicNumConverter.chineseToArabic("十"));
        assertEquals(15, ChineseArabicNumConverter.chineseToArabic("十五"));
        assertEquals(100, ChineseArabicNumConverter.chineseToArabic("一百"));
        assertEquals(250, ChineseArabicNumConverter.chineseToArabic("二百五十"));
        assertEquals(356, ChineseArabicNumConverter.chineseToArabic("三百五十六"));
        assertEquals(1000, ChineseArabicNumConverter.chineseToArabic("一千"));
    }

    @Test
    public void testBasicArabicNumbers() {
        assertEquals(0, ChineseArabicNumConverter.chineseToArabic("0"));
        assertEquals(1, ChineseArabicNumConverter.chineseToArabic("1"));
        assertEquals(10, ChineseArabicNumConverter.chineseToArabic("10"));
        assertEquals(15, ChineseArabicNumConverter.chineseToArabic("15"));
        assertEquals(100, ChineseArabicNumConverter.chineseToArabic("100"));
        assertEquals(250, ChineseArabicNumConverter.chineseToArabic("250"));
        assertEquals(356, ChineseArabicNumConverter.chineseToArabic("356"));
        assertEquals(1000, ChineseArabicNumConverter.chineseToArabic("1000"));
        assertEquals(1000, ChineseArabicNumConverter.chineseToArabic("1千"));
        assertEquals(3000, ChineseArabicNumConverter.chineseToArabic("3千"));
    }

    @Test
    public void testComplexNumbers() {
        assertEquals(1234, ChineseArabicNumConverter.chineseToArabic("一千二百三十四"));
        assertEquals(1005, ChineseArabicNumConverter.chineseToArabic("一千零五"));
        assertEquals(1056, ChineseArabicNumConverter.chineseToArabic("一千零五十六"));
        assertEquals(11000, ChineseArabicNumConverter.chineseToArabic("一万一"));
        assertEquals(123456789, ChineseArabicNumConverter.chineseToArabic("一亿二千三百四十五万六千七百八十九"));
        assertEquals(20050300, ChineseArabicNumConverter.chineseToArabic("二千零五万零三百"));
        assertEquals(10100000, ChineseArabicNumConverter.chineseToArabic("一千零一十万"));
        assertEquals(98765432, ChineseArabicNumConverter.chineseToArabic("九千八百七十六万五千四百三十二"));
        assertEquals(2131231, ChineseArabicNumConverter.chineseToArabic("两百一十三万一千二百三十一"));
        assertEquals(35000060, ChineseArabicNumConverter.chineseToArabic("三千五百万零六十"));
        assertEquals(15000, ChineseArabicNumConverter.chineseToArabic("一万五"));
        assertEquals(1500, ChineseArabicNumConverter.chineseToArabic("一千五"));
    }

    @Test
    public void testComplexArabicNumbers() {
        assertEquals(1234, ChineseArabicNumConverter.chineseToArabic("1234"));
        assertEquals(1005, ChineseArabicNumConverter.chineseToArabic("1005"));
        assertEquals(1056, ChineseArabicNumConverter.chineseToArabic("1056"));
        assertEquals(11000, ChineseArabicNumConverter.chineseToArabic("11000"));
        assertEquals(11000, ChineseArabicNumConverter.chineseToArabic("1万1"));
        assertEquals(123456789, ChineseArabicNumConverter.chineseToArabic("123456789"));
        assertEquals(123450000, ChineseArabicNumConverter.chineseToArabic("1亿2345万"));
        assertEquals(20050300, ChineseArabicNumConverter.chineseToArabic("20050300"));
        assertEquals(20050000, ChineseArabicNumConverter.chineseToArabic("2005万"));
        assertEquals(10100000, ChineseArabicNumConverter.chineseToArabic("10100000"));
        assertEquals(10100000, ChineseArabicNumConverter.chineseToArabic("1010万"));
        assertEquals(98765432, ChineseArabicNumConverter.chineseToArabic("98765432"));
        assertEquals(98760000, ChineseArabicNumConverter.chineseToArabic("9876万"));
        assertEquals(2131231, ChineseArabicNumConverter.chineseToArabic("2131231"));
        assertEquals(35000060, ChineseArabicNumConverter.chineseToArabic("35000060"));
        assertEquals(15000, ChineseArabicNumConverter.chineseToArabic("15000"));
        assertEquals(15000, ChineseArabicNumConverter.chineseToArabic("1万5"));
        assertEquals(1500, ChineseArabicNumConverter.chineseToArabic("1500"));
        assertEquals(1500, ChineseArabicNumConverter.chineseToArabic("1千5"));
        assertEquals(1500, ChineseArabicNumConverter.chineseToArabic("1千5百"));
        assertEquals(1500, ChineseArabicNumConverter.chineseToArabic("1千500"));
    }

    @Test
    public void testEdgeCases() {
        assertEquals(10000, ChineseArabicNumConverter.chineseToArabic("一万"));
        assertEquals(100000, ChineseArabicNumConverter.chineseToArabic("十万"));
        assertEquals(1000000, ChineseArabicNumConverter.chineseToArabic("一百万"));
        assertEquals(10000000, ChineseArabicNumConverter.chineseToArabic("一千万"));
        assertEquals(100000000, ChineseArabicNumConverter.chineseToArabic("一亿"));
        assertEquals(99999999, ChineseArabicNumConverter.chineseToArabic("九千九百九十九万九千九百九十九"));
    }

    @Test
    public void testAlternativeSyntax() {
        // 测试 "两" 的用法
        assertEquals(2000, ChineseArabicNumConverter.chineseToArabic("两千"));
        assertEquals(20000, ChineseArabicNumConverter.chineseToArabic("两万"));
        // 测试无单位省略
        assertEquals(15, ChineseArabicNumConverter.chineseToArabic("一十五"));
        assertEquals(315, ChineseArabicNumConverter.chineseToArabic("三百一十五"));
        assertEquals(1235, ChineseArabicNumConverter.chineseToArabic("一千两百三十五"));
    }

    @Test
    public void testZeroHandling() {
        assertEquals(105, ChineseArabicNumConverter.chineseToArabic("一百零五"));
        assertEquals(1006, ChineseArabicNumConverter.chineseToArabic("一千零六"));
        assertEquals(30000050, ChineseArabicNumConverter.chineseToArabic("三千万零五十"));
    }
}