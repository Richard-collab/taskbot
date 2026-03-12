package org.example.chat.bean.baize;

import java.util.Map;
import java.util.stream.Collectors;

public class TenantInfoTool {

    private Map<Integer, String> tenantId2tenantNo;

    private Map<Integer, String> tenantId2tenantName;
    private Map<String, Integer> tenantNo2tenantId;
    private Map<String, Integer> tenantName2tenantId;

    public TenantInfoTool(Map<Integer, String> tenantId2tenantNo, Map<Integer, String> tenantId2tenantName) {
        this.tenantId2tenantNo = tenantId2tenantNo;
        this.tenantId2tenantName = tenantId2tenantName;
        this.tenantNo2tenantId = tenantId2tenantNo.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        this.tenantName2tenantId = tenantId2tenantName.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public Integer getTenantIdByTenantNo(String tenantNo) {
        return tenantNo2tenantId.get(tenantNo);
    }

    public Integer getTenantIdByTenantName(String tenantName) {
        return tenantName2tenantId.get(tenantName);
    }

    public String getTenantNoByTenantId(Integer tenantId) {
        return tenantId2tenantNo.get(tenantId);
    }

    public String getTenantNoByTenantName(String tenantName) {
        Integer tenantId = tenantName2tenantId.get(tenantName);
        if (tenantId == null) {
            return null;
        } else {
            return tenantId2tenantNo.get(tenantId);
        }
    }

    public String getTenantNameByTenantId(String tenantId) {
        return tenantId2tenantName.get(tenantId);
    }

    public String getTenantNameByTenantNo(String tenantNo) {
        Integer tenantId = tenantNo2tenantId.get(tenantNo);
        if (tenantId == null) {
            return null;
        } else {
            return tenantId2tenantName.get(tenantId);
        }
    }
}
