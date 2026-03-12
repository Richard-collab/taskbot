package org.example.chat.bean.baize.script;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 语义类，对应研发的 SinglePhrase 类
 */
@Getter
@Setter
public class SemanticPhrase implements Serializable {

    private Long id; // 意图相关
    private String semantic; // 意图相关
    private String word; // 短语相关
    private String pattern;
    private Boolean regex;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
