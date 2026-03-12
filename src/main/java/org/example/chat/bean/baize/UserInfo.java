package org.example.chat.bean.baize;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class UserInfo implements Serializable {

    private int id;
    private String createTime;
    private String updateTime;
	private String account;
    private String name;
    private int roleId;
    private String phone;
    private Gender gender;
    private String department;
    private String email;
    private String address;
    private Integer tenantId;
    private int accountType;
    private boolean accountEnable;
    private String note;
    private String groupId;
    private boolean isTenantManager;
    private String callbackUrl;
    private String clueCallbackUrl;
    private String taskTimeRange; // 用英文逗号分隔起讫时间，如"08:00,20:00"
    private String accessKey;
    private String secretKey;
    private String antAccessKey;
    private String antSecretKey;
    private String endPoint;
    private String protocol;
    private String productInstanceId;
    private Boolean isForEncryptionPhones;
    private List<OutboundType> callBackRange;
    private List<OutboundType> dataStatisticRange;
    private String latestLoginTime; // 如"2025-08-06 14:21:40"
    private String adminRole;
//            "isPushDialogContent": null,
    private String smsCallbackUrl;
    private String taskCallbackUrl;
    private String callDataCallBackUrl;
    private String callSmsCallBackUrl;
    private String callUpdateCallBackUrl;
    private String callMCallBackUrl;
    private List<String> callbackStatusConfig;
    private List<String> callbackFieldConfig;
    private Boolean isForEncryptionAgain;
    private String smsMoCallbackUrl;

    public List<CallbackField> getCallbackFieldList() {
        if (callbackFieldConfig == null) {
            return null;
        }
        List<CallbackField> callbackFieldList = new ArrayList<>();
        for (String value: callbackFieldConfig) {
            for (OutboundCallbackField callbackField: OutboundCallbackField.values()) {
                if (Objects.equals(callbackField.name(), value)) {
                    callbackFieldList.add(OutboundCallbackField.valueOf(value));
                }
                break;
            }
            for (SmsCallbackField callbackField: SmsCallbackField.values()) {
                if (Objects.equals(callbackField.name(), value)) {
                    callbackFieldList.add(SmsCallbackField.valueOf(value));
                }
                break;
            }
        }
        return callbackFieldList;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
