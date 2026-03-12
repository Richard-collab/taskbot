package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 开放范围
 */
public enum CorpusReturnType {

    REPLAY("重播"),

    UNDERTAKE("承接"),

    PLAY_DEFAULT("默认"),
    ;

    private String caption;

    CorpusReturnType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static CorpusReturnType fromCaption(String caption) {
        CorpusReturnType result = null;
        for (CorpusReturnType value: CorpusReturnType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
