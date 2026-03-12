package org.example.chat.bean.baize;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class TaskTemplate implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String templateName; // 模板名称
    private String comment; // 模板说明
    private String taskName; // 任务名称
    private String tenantName; // 商户名称
    private String account; // 账号
    private Long speechCraftId; // 话术id
    private String speechCraftName; // 话术名称
    private String scriptStringId; // 话术uuid
    private int version; // 话术版本
    private String startWorkTimes; // 拨打时段开始时间（用英文逗号分隔，不带空格，如09:00,13:30）
    private String endWorkTimes; // 拨打时段结束时间（用英文逗号分隔，不带空格，如09:00,13:30）
    private int autoReCall; // 自动补呼，1：补呼，0：不补呼
//            "taskIncrId": null,
    private Integer callRatioType; // 分配方式，1：首呼优先分配，2：多轮呼叫按比例分配
    private Integer firstRecallTime; // 第一次补呼间隔（分钟）
    private Integer secondRecallTime; // 第二次补呼间隔（分钟）
    private String groupId;
    private TaskType taskType;
    private List<Integer> callTeamIds;
    private PushType callTeamPushType; // 推送方式
    private HandleType callTeamHandleType; // 处理方式
    private Float lineRatio; // 集线比
    @SerializedName("occupyRate")
    private Integer occupyRateId; // 坐席占用等级，1：低，3：中，6：高
    private Float virtualSeatRatio; // 虚拟坐席系数
    @SerializedName("scriptSms")
    private List<ScriptSmsInfo> scriptSmsInfoList; // 触发短信，话术中的短信
    @SerializedName("hangUpSms")
    private List<HangupSmsInfo> hangupSmsInfoList; // 挂机短信
    @SerializedName("hangUpExcluded")
    private List<String> hangupExcludedLabelList; // 发送排除标签
    @SerializedName("variableSms")
    private List<SmsVariable> smsVariableList; // 挂机短信中涉及到的变量
    private int nextDayCall; // 隔日续呼，1：续呼，0：不续呼
    private List<Integer> tenantBlackList; // 商户黑名单id列表
    private String allRestrictProvince; // 屏蔽省份（用英文逗号分隔，不带空格
    private String allRestrictCity; // 屏蔽城市（用英文逗号分隔，不带空格
    private String ydRestrictProvince;
    private String ydRestrictCity;
    private String ltRestrictProvince;
    private String ltRestrictCity;
    private String dxRestrictCity;
    private String dxRestrictProvince;
    private String unknownRestrictCity;
    private String unknownRestrictProvince;
    private String templateStatus; // 启用："0"，停用："1"
    private List<String> startWorkTimeList; // 拨打时段开始时间列表（如["09:00", "13:00"]）
    private List<String> endWorkTimeList; // 拨打时段结束时间列表（如["12:00", "19:00"]）

    public OccupyRate getOccupyRate() {
         // 坐席占用等级，1：低，3：中，6：高
        return OccupyRate.fromId(occupyRateId);
    }

    public String getStrCallRatioType() {
        // 分配方式，1：首呼优先分配，2：多轮呼叫按比例分配
        String strCallRatioType;
        switch (callRatioType) {
            case 1: {
                strCallRatioType = "首呼优先分配";
                break;
            }
            case 2: {
                strCallRatioType = "多轮呼叫按比例分配";
                break;
            }
            default:
                strCallRatioType = null;
        }
        return strCallRatioType;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
