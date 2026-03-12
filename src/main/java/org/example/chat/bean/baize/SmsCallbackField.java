package org.example.chat.bean.baize;

import java.util.Objects;

public enum SmsCallbackField implements CallbackField {

    smsPhone("手机号"),
    smsFullName("姓名"),
    smsCompany("公司"),
    smsRemarks("备注"),
    ;

    private String caption;

    SmsCallbackField(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static SmsCallbackField fromCaption(String caption) {
        SmsCallbackField result = null;
        for (SmsCallbackField callbackField: SmsCallbackField.values()) {
            if (Objects.equals(callbackField.getCaption(), caption)) {
                result = callbackField;
                break;
            }
        }
        return result;
    }
}
