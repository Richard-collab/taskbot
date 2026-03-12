package org.example.chat.bean.baize;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
public class LineGateway implements Serializable  {

    private int id;
    private String createTime;
    private String updateTime;
    private String gatewayNumber;
    private String name;
    private int concurrentLimit;
    private List<String> supplyLineNumbers;
    private Integer caps; // x次/秒
    private List<String> callingRestrictions; // 线路拨打限制，["3-12"] 表示 3次/12小时
    private List<String> dialingRestrictions; // 线路拨通限制，["3-12"] 表示 3次/12小时
    private String notes;
    private List<SupplyLine> supplyLines;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
