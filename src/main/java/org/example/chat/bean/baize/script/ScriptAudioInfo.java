package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 主流程语料
 */
@Getter
public class ScriptAudioInfo implements Serializable {

    private Integer uploadNums;
    private Integer totalNums;
    private List<ScriptUnitContent> scriptUnitContents;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
