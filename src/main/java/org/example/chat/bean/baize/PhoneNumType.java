package org.example.chat.bean.baize;

import java.util.Objects;

public enum PhoneNumType {

    CIPHERTEXT("加密"),
    PLAINTEXT_ALL_THE_TIME("全程明文"),
    PLAINTEXT_AFTER_DECRYPTION("解密明文"),
    ;

    private String caption;

    PhoneNumType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static PhoneNumType fromCaption(String caption) {
        PhoneNumType result = null;
        for (PhoneNumType phoneNumType: PhoneNumType.values()) {
            if (Objects.equals(phoneNumType.getCaption(), caption)) {
                result = phoneNumType;
                break;
            }
        }
        return result;
    }
}
