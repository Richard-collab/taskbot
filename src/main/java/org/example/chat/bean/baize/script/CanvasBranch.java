package org.example.chat.bean.baize.script;

import lombok.*;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 主流程节点，对应研发的 ScriptCanvas 类
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CanvasBranch implements Serializable {

    private Long branchId;

    private Long preCorpusId;

    private Long nextCorpusId;

    private String name;

    private String branchContent;

    private String color;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
