package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 优先级类型
 */
public enum PriorType {

    QUERY_BRANCH("查询分支"),

    GENERAL_BRANCH("普通分支"),

    KNOWLEDGE_GROUPS("知识库分组"),

    MISS("未命中"),

    SILENCE("沉默"),
    ;

    private String caption;

    PriorType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static PriorType fromCaption(String caption) {
        PriorType result = null;
        for (PriorType value: PriorType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
