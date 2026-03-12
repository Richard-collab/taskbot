package org.example.chat.bean.baize;

import java.util.Objects;

public enum TaskStatus {

    UNEXCUTED("待执行"),
    ONGONIG("进行中"),
    INCOMPLETE("未完成"),
    STOPPED("已停止")
    ;

    private String caption;

    TaskStatus(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static TaskStatus fromCaption(String caption) {
        TaskStatus result = null;
        for (TaskStatus taskStatus: TaskStatus.values()) {
            if (Objects.equals(taskStatus.getCaption(), caption)) {
                result = taskStatus;
                break;
            }
        }
        return result;
    }

}
