package org.example.instruction.bean;

import java.util.Objects;

public enum ReplaceType {

    SCRIPT_NAME("语料名称"),
    SCRIPT_CONTENT("语料内容"),
    SCRIPT_NAME_AND_CONTENT("语料名称和内容"),
    ;

    private String caption;

    ReplaceType(String caption) {
        this.caption = caption;
    }

    public static ReplaceType fromCaption(String caption) {
        ReplaceType result = null;
        for (ReplaceType value: ReplaceType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }

    public String getCaption() {
        return caption;
    }
}
