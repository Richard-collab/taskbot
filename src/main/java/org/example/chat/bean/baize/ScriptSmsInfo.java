package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
@Setter
public class ScriptSmsInfo implements Serializable {

    private String triggerName; // 触发点
    private int smsTemplateId; // 短信模板id

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
