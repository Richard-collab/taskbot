package org.example.chat.bean.baize;

import java.util.Objects;

public enum SmsAccountBusinessType {

    MASS_SENDING("群发"),
    HANGUP_SMS("挂短"),
    ;

    private String caption;

    SmsAccountBusinessType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static TaskType fromCaption(String caption) {
        TaskType result = null;
        for (TaskType taskType: TaskType.values()) {
            if (Objects.equals(taskType.getCaption(), caption)) {
                result = taskType;
                break;
            }
        }
        return result;
    }
}
