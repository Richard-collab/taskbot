package org.example.chat.bean.baize.script;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.example.chat.bean.baize.ScriptStatus;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 话术，对应研发的 Script 类
 */
@Getter
public class Script implements Serializable {

    private Long id;
    private String createTime;
    private String updateTime;
    private String scriptStringId; // 话术uuid
    private String scriptName;
    private Integer version; // 话术版本
    private Integer latestVersion; // 话术最新版本
    private Boolean isDeleted;
    private Long headCorpusId;
    private ScriptStatus status;
    private String primaryIndustry;
    private String secondaryIndustry;
    private Long secondaryIndustryId;
    private Integer maxSilenceCount;
    private Long triggerSilenceHangupTagId;
    private Set<String> smsTriggerNames; // 短信触发点名称列表json
    private String lockAccount;
    private String ownerAccount;
    private List<String> watchAccounts;
//    private List<String> usedValueNames;
    private String verifyStatus;
    private Boolean editPermission;
    private String remark; // 备注
    private String lastUsingDate; // 如 "2025-07-10"
    private Boolean transferHuman; // 是否转人工
    private Boolean multiContentVersion;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
