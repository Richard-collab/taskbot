package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 开放范围
 */
public enum QueryType {

    INDUSTRY_QUERY("业务问答"),
    COMMON_QUERY( "通用问答"),
    ;

    private String caption;

    QueryType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static QueryType fromCaption(String caption) {
        QueryType result = null;
        for (QueryType value: QueryType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
