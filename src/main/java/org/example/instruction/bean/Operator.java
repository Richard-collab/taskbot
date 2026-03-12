package org.example.instruction.bean;

import java.util.Arrays;
import java.util.List;

public enum Operator {

    CHINA_MOBILE("移动"),
    CHINA_TELECOM("电信"),
    CHINA_UNICOM("联通"),
    ALL_OPERATOR("全部")
    ;

    private String caption;

    Operator(String caption) {
        this.caption = caption;
    }

    public static Operator fromCaption(String caption) {
        if (caption == null) {
            return null;
        }
        Operator operator =  null;
        switch (caption) {
            case "移动":
            case "中国移动": {
                operator = CHINA_MOBILE;
                break;
            }
            case "电信":
            case "中国电信": {
                operator = CHINA_TELECOM;
                break;
            }
            case "联通":
            case "中国联通": {
                operator = CHINA_UNICOM;
                break;
            }
            case "全部": {
                operator = ALL_OPERATOR;
                break;
            }
            default:
                break;
        }
        return operator;
    }

    public String getCaption() {
        return caption;
    }
}
