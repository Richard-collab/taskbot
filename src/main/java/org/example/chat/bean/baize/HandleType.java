package org.example.chat.bean.baize;

import java.util.Objects;

public enum HandleType {

    ANSWER("接听"),
    MONITOR("监听"),
    ;


    private String caption;

    HandleType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static HandleType fromCaption(String caption) {
        HandleType result = null;
        for (HandleType value: HandleType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
