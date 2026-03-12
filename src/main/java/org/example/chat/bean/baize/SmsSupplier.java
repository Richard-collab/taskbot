package org.example.chat.bean.baize;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class SmsSupplier implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String supplierNumber;
    private String supplierName;
    private String supplierProfile;
    private String supplierAddress;
    private String contactName;
    private String phoneNumber;
    private String email;
    private String duty;
    private String contactAddress;
    private EnableStatus cooperationStatus;
    private String notes;
    private SmsProtocol smsProtocol;
    private String configInfo;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
