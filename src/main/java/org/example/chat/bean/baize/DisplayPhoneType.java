package org.example.chat.bean.baize;

import java.util.Objects;

public enum DisplayPhoneType {

    MOBILE_PHONE("手机号"),
    FIXED_PHONE("固定电话"),
    ;

    private String caption;

    DisplayPhoneType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static DisplayPhoneType fromCaption(String caption) {
        DisplayPhoneType result = null;
        for (DisplayPhoneType value: DisplayPhoneType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
