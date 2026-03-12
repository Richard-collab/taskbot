package org.example.chat.bean.baize.script;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 对应研发的 SemListPack 类
 */
@Getter
@Setter
public class SemListPack implements Serializable {

    private SemListPackType semListPackType;
    private List<SemanticPhrase> singlePhraseList;
    private Integer maxNum;
    private Integer minNum;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
