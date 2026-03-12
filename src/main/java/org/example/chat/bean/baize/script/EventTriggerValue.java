package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
public class EventTriggerValue implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private String name; // 事件值名称
    private String eventTriggerType; // 事件模板
    private List<String> eventTriggerArgValues; // 事件模板对应的参数
    private String note; // 备注


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
