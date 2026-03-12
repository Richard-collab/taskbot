package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TenantLine implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String lineNumber;
    private String lineName;
    private LineType lineType;
    private int tenantId;
    private EnableStatus enableStatus;
    private int concurrentLimit;
    private int lineRemainConcurrent;
    private String notes;
    private String groupId;
    private List<String> secondIndustries;
    private List<SupplyLineGroup> supplyLineGroups;
    private int adminId;


    public String toDescription() {
        return "商户线路【" + lineName + "】（" + lineNumber + "）";
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
