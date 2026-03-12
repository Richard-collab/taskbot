package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
public class EventTriggerType implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private String eventTriggerType; // 事件模板
    private List<String> eventTriggerArgs; // 事件模板参数名称列表


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
