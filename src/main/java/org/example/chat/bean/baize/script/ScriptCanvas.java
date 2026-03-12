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
public class ScriptCanvas implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private String name;
    private Long preCanvasId;
    private Long scriptId;
    private Boolean isMasterCanvas;
    private Long headCorpusId;
    private Integer weight;
    private Map<Long, CanvasCorpus> canvasCorpusDataMap;
    private Map<Long, CanvasBranch> canvasBranchDataMap;
    private Long knowledgeGroupId; //知识库
    private Boolean isOpenContext; // 语境类型：true：开放，false：封闭
    private OpenScopeType openScopeType; // 开放范围
    private List<Long> groupOpenScope; // 开放范围对应的知识库分组id


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
