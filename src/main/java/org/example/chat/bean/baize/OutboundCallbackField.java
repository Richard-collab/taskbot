package org.example.chat.bean.baize;

import java.util.Objects;

public enum OutboundCallbackField implements CallbackField {

    phone("手机号"),
    name("姓名"),
    company("公司"),
    remarks("备注"),
    ;

    private String caption;

    OutboundCallbackField(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static OutboundCallbackField fromCaption(String caption) {
        OutboundCallbackField result = null;
        for (OutboundCallbackField callbackField: OutboundCallbackField.values()) {
            if (Objects.equals(callbackField.getCaption(), caption)) {
                result = callbackField;
                break;
            }
        }
        return result;
    }
}
