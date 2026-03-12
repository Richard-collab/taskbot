package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 开放范围，对应研发的 OpenScopeType
 */
public enum OpenScopeType {

    ALL("全部"),
    CUSTOM("自定义"),
    SUCCEED_CONTEXT("继承原语境")
    ;

    private String caption;

    OpenScopeType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static OpenScopeType fromCaption(String caption) {
        OpenScopeType result = null;
        for (OpenScopeType value: OpenScopeType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
