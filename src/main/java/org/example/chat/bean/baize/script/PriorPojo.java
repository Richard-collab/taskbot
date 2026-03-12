package org.example.chat.bean.baize.script;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 优先级对象，对应研发的 PriorPojo 类
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PriorPojo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 返回排除
     */
    private Boolean backExcluded;

    /**
     * 优先级类型
     */
    private PriorType type;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
