package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
public class InfoQueryValue implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private Integer weight;
    private String value;
    private String definition;
    private String note;
    private Long infoQueryKeyId;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
