package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
public class Semantic implements Serializable {

    private Long semanticId;
    private String semantic;
    private Long semanticLabelId;
    private String semanticLabel;
    private List<String> semanticLabelList;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
