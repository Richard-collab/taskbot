package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 开放范围，对应研发的 SemListPackType
 */
public enum SemListPackType {

    HIT_SEM("本句语义命中"),
    HISTORY_SEM("历史语义命中"),
    EXTRA_WORD("本句短语命中")
    ;

    private String caption;

    SemListPackType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static SemListPackType fromCaption(String caption) {
        SemListPackType result = null;
        for (SemListPackType value: SemListPackType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
