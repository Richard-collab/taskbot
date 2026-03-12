package org.example.chat.bean.baize.script;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 语义触发条件，对应研发的 SemConditionEntity 类
 */
@Getter
@Setter
public class SemCondition implements Serializable {

    private List<SemListPack> semListPacks; // 这些 SemListPack 之间是 and 关系

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
