package org.example.chat.bean.baize;

import java.util.Objects;

public enum ScriptStatus {

    EDIT("编辑中"),
    VERIFY("审核中"),
    PREVIEW("预发布"),
    ACTIVE("生效中"),
    REJECT("已驳回"),
    STOP("已停用"),
    ;

    private String caption;

    ScriptStatus(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static ScriptStatus fromCaption(String caption) {
        ScriptStatus result = null;
        for (ScriptStatus value: ScriptStatus.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
