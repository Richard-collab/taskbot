package org.example.chat.bean.baize;

import java.util.Objects;

public enum LineAccessType {

    DIRECT_CONNECT("直连"),
    INDIRECT_CONNECT("间连"),
    ;

    private String caption;

    LineAccessType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static LineAccessType fromCaption(String caption) {
        LineAccessType result = null;
        for (LineAccessType value: LineAccessType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}

