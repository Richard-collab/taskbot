package org.example.chat.bean.baize.script;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 画布语料，对应研发的 CanvasCorpusData 类
 */
@Getter
@Setter
public class CanvasCorpus implements Serializable {

    private Long corpusId;
    private Integer corX;
    private Integer corY;
    private String name; // 语料名称
    private String content; // 语料内容
    private List<ScriptBranch> branches; // 分支
    private CorpusType corpusType; // 语料类型
    private ConnectType connectType; // 连接到
    private Long connectCorpusId; //主动流程
    private Long nextCanvasId; //
    private IntentionClass aiIntentionType; // 意向分类
    private List<Long> eventTriggerValueIds;
    private Boolean listenInOrTakeOver;
    private String smsTriggerName; // 触发点名称


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
