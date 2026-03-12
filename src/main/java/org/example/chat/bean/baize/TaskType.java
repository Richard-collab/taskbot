package org.example.chat.bean.baize;

import java.util.Objects;

public enum TaskType {

    AI_AUTO("纯AI", LineType.AI_OUTBOUND_CALL),
    AI_MANUAL("人机协同", LineType.AI_OUTBOUND_CALL),
    MANUAL_DIRECT("人工直呼", LineType.MANUAL_DIRECT_CALL),
    ;

    private String caption;
    private LineType lineType;

    TaskType(String caption, LineType lineType) {
        this.caption = caption;
        this.lineType = lineType;
    }

    public String getCaption() {
        return caption;
    }

    public LineType getLineType() {
        return lineType;
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
