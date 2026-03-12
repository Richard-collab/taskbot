package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
public class IntentionLabel implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private Long scriptId;
    private String labelName;
    private Integer sequence;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
