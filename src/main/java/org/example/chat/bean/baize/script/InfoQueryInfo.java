package org.example.chat.bean.baize.script;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 查询分支的查询事件
 */
@Getter
public class InfoQueryInfo implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private Long scriptLongId;
    private String infoFieldName;
    private String fieldDefinition;
    private List<Long> infoQueryValueIds;
    private List<InfoQueryValue> infoQueryValues;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
