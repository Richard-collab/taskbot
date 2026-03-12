package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 开放范围
 */
public enum ResponseType {

    POLITE_RESPONSE("礼貌回复"),

    PREEMPT_RESPONSE("抢占回复"),
    ;

    private String caption;

    ResponseType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static ResponseType fromCaption(String caption) {
        ResponseType result = null;
        for (ResponseType value: ResponseType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
