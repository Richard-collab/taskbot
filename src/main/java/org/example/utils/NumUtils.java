package org.example.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class NumUtils {

    private static final float deltaFloat = 1e-7f;
    private static final double deltaDouble = 1e-7;
    public static boolean equals(float a, float b) {
        return Math.abs(a - b) < deltaFloat;
    }

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < deltaDouble;
    }

    public static boolean isArabicNum(String value) {
        Pattern pattern = Pattern.compile("^-?[0-9]+(\\.[0-9]*)?$");
        return pattern.matcher(value).matches();
    }

    public static double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static String roundStringFormat(double value, int scale) {
        return String.format("%." + scale + "f", value);
    }

    public static void main(String[] args) {
        System.out.println(roundStringFormat(1.1234567, 5));
//        System.out.println(arabicNumToChinese("一百一十一万零五百"));
//        System.out.println(arabicNumToChinese(1100500));
    }
}
