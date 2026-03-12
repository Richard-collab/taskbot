package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 语料类型
 */
public enum ContentType {

    TEXT( "文本类型"),

    VARIABLE("变量类型");
    ;

    private String caption;

    ContentType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static ContentType fromCaption(String caption) {
        ContentType result = null;
        for (ContentType value: ContentType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
