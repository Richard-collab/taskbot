package org.example.chat.bean.baize;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
public class RelatedScript implements Serializable {

    private Long relatedScriptId;
    private String groupId;
    private Long scriptId;
    private String scriptStringId; // 话术uuid
    private String scriptName;
    private Integer tenantId;
    @SerializedName("active")
    private ScriptStatus status;
    private Integer version; // 话术版本
    @SerializedName("secondIndustry")
    private String secondaryIndustry;
    @SerializedName("smsTriggerNames")
    private String smsTriggerNamesJson; // 短信触发点名称列表json

    public List<String> getSmsTriggerNameList() {
        try {
            return JsonUtils.fromJson(smsTriggerNamesJson, new TypeToken<List<String>>(){});
        } catch (Exception e) {
            return null;
        }
    }
}
