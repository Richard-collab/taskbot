package org.example.chat.bean.baize;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.instruction.bean.Operator;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
public class SmsAccount implements Serializable {

    private Integer id; // 创建时不填--
    private String createTime; // 创建时不填--
    private String updateTime; // 创建时不填--
    private Integer supplierId; // 供应商id
    private String supplierNumber; // 供应商编号
    private String supplierName; // 供应商名称
    private String supplierProfile; // 供应商简称
    private String account; // 所属商户账号
    private String groupId; // 所属商户groupId
    private String smsAccountNumber; // 短信账号编号
    private String smsAccountName; // 短信账号名称
    private String smsAccountRegisterCode; // 创建时不填--
    private Integer smsAccountRegisterId; // 创建时不填--
    private EnableStatus enableStatus; // 短信账号启用状态
    private boolean pending; // 短信账号是否挂起：true挂起，false未挂起
    private String notes; // 短信账号运营备注
    private SmsProtocol smsProtocol; // 对接协议
    private Integer version; // 协议版本
    @SerializedName("connectAddress")
    private String connectIP; // 对接地址
    private Integer connectPort; // 对接端口
    private String extensionCode; // 扩展码
    private String connectAccount; // 账号
    private String password; // 密码
    private String srcId; // 接入码
    private String serviceId; // 服务id
    private Integer maxChannels; // 最大连接数
    private boolean isReturn; // 是否回执
    private Integer returnTimeout; // 回执超时时间（秒）
    private Integer sendDelay; // 发送延迟（秒）
    private String configInfo; // 配置信息
    private Integer singleSubmitLimit; // 单次提交上限
    private String singleDaySubmitLimit; // 单日提交上限
    private Collection<String> secondIndustries; // 适用二级行业名称列表
    private SmsAccountBusinessType smsAccountBusinessType; // 适用业务
    private Integer submitSpeedLimit; // 提交速率限制
    private List<String> submitRestrictions; // 提交频率限制，如["3-24"]表示3次/24小时
    private List<String> sendRestrictions; // 发送频率限制，如["6-12"]表示6次/12小时
    private List<Integer> disableTimeSlots; // 时间限制
    private Integer billingCycle; // 计费方式，多少字/条
    private Float unitPrice; // 短信单价
    private Operator serviceProvider; // 支持运营商
    private List<String> cityCodes; // 支持省市区号列表
//    tenantSmsTemplates; // 创建时不填 --


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
