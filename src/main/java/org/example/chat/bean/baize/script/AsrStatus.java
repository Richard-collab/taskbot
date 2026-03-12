package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 语料类型
 */
public enum AsrStatus {

    IN_PROCESS("识别中"),

    DONE("已完成"),

    FAILED("失败");
    ;

    private String caption;

    AsrStatus(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static AsrStatus fromCaption(String caption) {
        AsrStatus result = null;
        for (AsrStatus value: AsrStatus.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
