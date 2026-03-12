package org.example.chat.bean.baize.script;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 语义、短语触发条件，对应研发的 SemCombineEntity 类
 */
@Getter
@Setter
public class SemCombineEntity implements Serializable {

    private List<SemCondition> satisfySemConditions; // 满足条件，这些 SatisfySemCondition 是 or 关系
    private List<SemCondition> excludeSemConditions; // 排除条件，这些 SatisfySemCondition 是 or 关系

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
