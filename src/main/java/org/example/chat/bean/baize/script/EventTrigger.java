package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * 对应研发的 EventTriggerForScript 类
 */
@Getter
public class EventTrigger implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private String eventName; // 事件名称
    private Map<String, EventValue> eventValuemap; // 事件值map<eventTriggerTemplateID, value>
    private String note;
    private Long scriptLongId; // 话术ID

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
