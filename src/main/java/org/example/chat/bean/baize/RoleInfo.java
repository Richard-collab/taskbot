package org.example.chat.bean.baize;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
public class RoleInfo implements Serializable {

    private int id;
    private String roleName;
    private List<String> authorityCodes;
    private Map<String, List<String>> authorityMap;
    private int tenantId;
    private int accountType;
    private String groupId;
    private List<String> ipList;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
