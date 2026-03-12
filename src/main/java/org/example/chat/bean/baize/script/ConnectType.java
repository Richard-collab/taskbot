package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 开放范围
 */
public enum ConnectType {

    SELECT_MASTER_PROCESS("指定主动流程"),
    RETURN_MASTER_PROCESS("回到主流程"),
    RETURN_ORIGIN("回到原语境"),
    HANG_UP("挂机"),
    ;

    private String caption;

    ConnectType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static ConnectType fromCaption(String caption) {
        ConnectType result = null;
        for (ConnectType value: ConnectType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
