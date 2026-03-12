package org.example.chat.bean.baize.script;

import lombok.*;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 优先级排序组合，对应研发的 PriorGroup 类
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class PriorGroup implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;

    /**
     * 话术id
     */
    private Long scriptId;

    /**
     * 语料id
     */
    private Long corpusId;

    private List<PriorPojo> priorList;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
