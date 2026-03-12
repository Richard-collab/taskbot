package org.example.chat.bean.baize;

import java.util.Objects;

public enum OutboundType {

    PURE_AI("AI外呼"),
    HUMAN_MACHINE("人机协同"),
    MANUAL_DIRECT("人工直呼"),
    ;

    private String caption;

    OutboundType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static OutboundType fromCaption(String caption) {
        OutboundType result = null;
        for (OutboundType outboundType : OutboundType.values()) {
            if (Objects.equals(outboundType.getCaption(), caption)) {
                result = outboundType;
                break;
            }
        }
        return result;
    }
}
