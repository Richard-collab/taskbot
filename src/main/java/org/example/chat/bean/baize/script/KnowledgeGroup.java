package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

/**
 * 对应研发的 KnowledgeGroup 类
 */
@Getter
public class KnowledgeGroup implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private Long scriptId;
    private String groupName;
    private Integer priority;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
