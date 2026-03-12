package org.example.chat.bean.baize;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.SerializationUtils;
import org.example.instruction.bean.Operator;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class SupplyLine implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String lineNumber;
    private String lineName;
    private LineType lineType;
    private int callLineSupplierId;
    private String callLineSupplierNumber;
    private String callLineSupplierName;
    private EnableStatus enableStatus;
    private String notes;
    private String masterCallNumber;
    private String displayCallNumber;
    private String prefix;
    private String registerIp;
    private Integer registerPort;
    private int concurrentLimit;
    @SerializedName("outboundTypes")
    private List<DisplayPhoneType> phoneTypeList;
    private List<String> secondIndustries;
    private LineAccessType lineAccessType;
    private List<Operator> serviceProviders;
    private List<String> cityCodes;
    private List<String> callingRestrictions; // 线路拨打限制，["3-12"] 表示 3次/12小时
    private List<String> dialingRestrictions; // 线路拨通限制，["3-12"] 表示 3次/12小时
    private List<String> callingRestrictionsGlobal; // 平台拨打限制，["3-12"] 表示 3次/12小时
    private List<String> dialingRestrictionsGlobal; // 平台拨通限制，["3-12"] 表示 3次/12小时
    private Integer caps; // x次/秒
    private float unitPrice; // 线路单价
    private int billingCycle; // 计费周期
    private List<Integer> disableTimeSlots; // 时间限制
    private List<Integer> lineGatewayIds; // 线路网关
    private List<Integer> lightPhoneIds; // 靓号限制
    private boolean isForEncryptionPhones;
    private List<CityCodeGroup> cityCodeGroups;
    private List<TenantLine> tenantLines;
    private List<LineGateway> lineGateways;
    private boolean pending;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

    public SupplyLine deepClone() {
        return SerializationUtils.clone(this);
    }
}
