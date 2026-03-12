package org.example.chat.bean.baize.script;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 主流程语料
 */
@Getter
@Setter
public class ScriptCorpus implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private String name;
    private String updateStatus;
    private Long scriptId;
    private String uploadStatus;
    private String uploadTime;
    private ConnectType connectType;
    private Long connectCorpusId;
    private Boolean isOpenContext;
    private OpenScopeType openScopeType;
    // 知识库分组开放范围
    private List<Long> groupOpenScope;
    private ResponseType responseType;
    private Long maxWaitingTime;
    private CorpusType corpusType;
    private Boolean isHead; // 是否画布的头节点语料
    private Boolean isTopHead; // 是否话术的头节点语料
    private Integer weight;
    private boolean deleted;
    private List<ScriptBranch> branchList;
    private List<IntentionLabel> aiLabels;
    private IntentionClass aiIntentionType;
    //是否知识库相关
    private Boolean isKnowledgeBase;
    // 知识库分组
    private Long knowledgeGroupId;
    // 基础问答 问答分类
    private QueryType queryType;
    private List<Long> eventTriggerValueIds;
    private Boolean listenInOrTakeOver;

    /**
     * 短信触发点名称
     */
    private String smsTriggerName;

    /**
     * 返回设置->true[默认未命中]  ->false[自定义播放]
     */
    private Boolean returnPlayDefault;

    /**
     * 返回设置为默认未命中(isReturnPlayDefault=true)的情况下,续播垫句(返回)ID
     */
    private Long preContinueCorpusIdForReturn;

    /**
     * 返回设置为自定义播放(isReturnPlayDefault=false)的情况下,播放完所有语句后, 语料的续播垫句ID
     */
    private Long preContinueCorpusIdBeforeDefault;

    /**
     * 返回设置为自定义播放(isReturnPlayDefault=false)的情况下,播放完所有语句后, 语料的续播类型
     */
    private CorpusReturnType corpusReturnType;

    /**
     * 返回设置为自定义播放(isReturnPlayDefault=false)的情况下,播放完所有语句后, 语料的承接语句ID
     */
    private Long preUndertakeCorpusId;

    /**
     * 语义条件
     */
    private SemCombineEntity semCombineEntity;

    /**
     * 语料用到的所有语义ID
     */
    private Set<Long> semanticIdsInUse;

    /**
     * 事件触发
     */
    private List<EventTriggerValue> eventTriggerValueList;

    /**
     * 多语句
     */
    private List<ScriptMultiContent> scriptMultiContents;

    /**
     * 匹配优先级
     */
    private PriorGroup priorGroup;

    /******以下是第八版本即将废弃的字段(欢迎填充)*******/
    private InterruptType interruptType;
    private Integer allowedInterruptTime;
    private String content;
    private Boolean isPlayed;
    private String audioPath;
    private List<Long> aiSemanticIds;
    private List<String> extraPhrases;



    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
