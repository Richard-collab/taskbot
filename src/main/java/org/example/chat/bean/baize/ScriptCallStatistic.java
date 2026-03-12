package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

@Getter
@Setter
public class ScriptCallStatistic {

    private String statisticDate;
    private String scriptStringId;
    private Integer totalNum;
    private Integer totalConnectNum;
    private OutboundType callType;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
