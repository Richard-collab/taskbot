package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
public class EventValue implements Serializable {

    private Long valueId;
    private String name;
    private String explanation;
    private String note;
    private String eventName;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
