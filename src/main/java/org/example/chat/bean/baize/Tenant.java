package org.example.chat.bean.baize;

import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
public class Tenant implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String tenantName;
    private String tenantShortName;
    private String tenantNo;
    private String contactName;
    private String contactPhone;
    private String contactMail;
    private String contactAddress;
    private String remark;
    private Integer status;
    private String callbackType; // 接口类型
    private CallbackMode callbackMode;
    private Object externalCallbackConfig;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
