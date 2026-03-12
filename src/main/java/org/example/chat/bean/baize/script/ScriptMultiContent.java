package org.example.chat.bean.baize.script;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 主流程语料
 */
@Getter
@Setter
public class ScriptMultiContent implements Serializable {

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

    /**
     * 排序
     */
    private int orders;

    /**
     * 最大名字后缀
     */
    private int maxNameSuffix;

    /**
     * 是否逻辑删除
     */
    private boolean deleted;

    private List<ScriptUnitContent> scriptUnitContents;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
