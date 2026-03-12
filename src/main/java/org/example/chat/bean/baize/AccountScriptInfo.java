package org.example.chat.bean.baize;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class AccountScriptInfo implements Serializable {

    private String createTime;
    private String updateTime;
    @SerializedName("id")
    private Integer tenantId;
    private String tenantName;
    private String tenantShortName;
    private String tenantNo;
    private String contactName;
    private String contactPhone;
    private String contactMail;
    private String contactAddress;
    private Integer status;
    private Object remark;
    List<RelatedScript> relatedScriptList;
    List<Object> relatedLineList;
}
