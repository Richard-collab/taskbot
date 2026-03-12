package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 语料类型
 */
public enum InterruptType {

    CAN_NOT_BE_INTERRUPTED("不允许打断"),

    SUPPORT_SOUND_INTERRUPT_NO_REPLY("支持发声打断但不回复"),

    SUPPORT_SOUND_INTERRUPT_WITH_REPLY("支持发声打断并回复"),

    SUPPORT_SEMANTIC_INTERRUPT_NO_REPLY("支持核心短语打断但不回复"),

    SUPPORT_SEMANTIC_INTERRUPT_WITH_REPLY("支持核心短语打断并回复"),

    HANG_UP_SPECIAL_CORPUS_INTERRUPT("挂机语料打断"),
    ;

    private String caption;

    InterruptType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static InterruptType fromCaption(String caption) {
        InterruptType result = null;
        for (InterruptType value: InterruptType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
