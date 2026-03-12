package org.example.chat.bean.baize;

import java.util.Objects;

public enum PushType {

    ROUND_ROBIN("轮询"),
    ;

    private String caption;

    PushType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static PushType fromCaption(String caption) {
        PushType result = null;
        for (PushType value: PushType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
