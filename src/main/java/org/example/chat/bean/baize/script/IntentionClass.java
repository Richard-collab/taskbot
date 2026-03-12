package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

/**
 * 对应研发的 AIIntentionType 类
 */
@Getter
public class IntentionClass implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private Long scriptId;
    private String intentionType;
    private Long sequence;
    private String intentionName;
    private String remark;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
