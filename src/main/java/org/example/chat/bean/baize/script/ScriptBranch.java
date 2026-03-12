package org.example.chat.bean.baize.script;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对应研发的 ScriptBranch 类
 */
@Getter
@Setter
public class ScriptBranch implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private String name; // 分支名称
    private Long scriptId;
    private Long preCorpusId;
    private Long nextCorpusId;
    private Boolean isPublished;
    private Map<String, String> queryField;
    private List<Long> infoQueryValueIds;
    private String color;
    private SemCombineEntity semCombineEntity; // 语义、短语触发条件
    private Set<Long> semanticIdsInUse; // 分支用到的所有语义，semCombineEntity 里涉及到的 singlePhraseList 里的 id
    private List<Long> aiSemanticIds;
    private List<String> extraPhrases; // semCombineEntity 里涉及到的 singlePhraseList 里的 word


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
