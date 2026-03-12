package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class HangupSmsInfo implements Serializable {

    private String intentionType; // 分类名称，如"A"
    private List<String> labelIds; // 标签列表，如["标签1", "标签2"]
    private int smsTemplateId; // 短信模板id
    private int triggerOrder; // 触发优先级，从0开始，越小优先级越高

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
