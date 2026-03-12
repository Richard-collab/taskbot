package org.example.chat.bean.baize.script;

import java.util.Objects;

/**
 * 语料类型
 */
public enum CorpusType {

    MASTER_ORDINARY("主动流程-普通语料"),
    MASTER_CONNECT("主动流程-连接语料"),
    KNOWLEDGE_BASE_QA("知识库-基本问答"),
    KNOWLEDGE_ORDINARY("深层沟通-普通语料"),
    KNOWLEDGE_CONNECT("深层沟通-连接语料"),
    FUNC_PRIOR_QA("功能话术-最高优先"),
    FUNC_REPEAT("功能话术-重复语料"),
    FUNC_SILENCE("功能话术-沉默语料"),
    PRE_INTERRUPT("功能话术-打断垫句"),
    PRE_CONTINUE("功能话术-续播垫句"),
    PRE_UNDERTAKE("功能话术-承接语料"),
    ;

    private String caption;

    CorpusType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static CorpusType fromCaption(String caption) {
        CorpusType result = null;
        for (CorpusType value: CorpusType.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }
}
