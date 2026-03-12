package org.example.chat.bean.baize;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class AiTask implements Serializable {

    private long id;
    private String createTime;
    private String updateTime;
    private String groupId;
    private String taskName;
    private int phoneNum; // 导入名单数
    private String tenantName;
    private int calledPhoneNum; // 呼叫名单数
    private int callingPhoneNum; // 首呼剩余名单数
    private int recallingPhoneNum; // 补呼剩余名单数
    private int finishedPhoneNum; // 呼叫名单数
    private int putThroughPhoneNum; // 接通电话数
    private int callCycle;
    private Integer calledPhoneRate;
    private Integer finishedPhoneRate; // 实际该值恒为null
    private Float putThroughPhoneRate;
    @SerializedName("aiAnswerNum")
    private int concurrency; // 并发数
    private String ifSendSms;
    private String callStatus;
//    "phoneOpPercent": null,
    private String batchStatus;
    private String speechCraftName;
    private Long speechCraftId;
    private String scriptStringId;
    private int version;
    private String lineName;
    private String lineCode;
    private int lineId;
    private String startWorkTimes;
    private String endWorkTimes;
    private int autoReCall;
    private int callRatioType;
    private int firstRecallTime;
    private int secondRecallTime;
    private String allRestrictProvince;
    private String allRestrictCity;
    private String ydRestrictProvince;
    private String ydRestrictCity;
    private String ltRestrictProvince;
    private String ltRestrictCity;
    private String dxRestrictCity;
    private String dxRestrictProvince;
    private String virtualRestrictCity;
    private String virtualRestrictProvince;
    private String unknownRestrictCity;
    private String unknownRestrictProvince;
    private int feeMinute;
    private String taskStartTime;
    private String taskEndTime;
    @SerializedName("isAutoStop")
    private int isAutoStop; // 是否止损的任务，0：非止损，1：止损
    private int callRecordNum; // 去除补偿的外呼数
    private int phoneIntentionNum;
    private List<Integer> callTeamIds;
    private TaskType taskType;
    private PushType callTeamPushType; // 推送方式
    private HandleType callTeamHandleType; // 处理方式
    private Float lineRatio; // 集线比
    private Integer occupyRate; // 坐席占用等级，1：低，3：中，6：高
//                "taskClueIds": null,
    private Integer ifLock; // 任务是否被锁定：1：锁定，null：未锁定
    private Float virtualSeatRatio; // 虚拟坐席系数
    private String programId;
    private String productId;
    private String industrySecondFieldId;
    private String expectedFinishTime;
    private List<Object> scriptSms;
    private List<Map<String, Object>> hangUpSms;
    @SerializedName("hangUpExcluded")
    private List<String> hangupExcludedLabelList; // 发送排除标签
    private List<Map<String, String>> variableSms;
    private int smsTemplateAbnormal;
    private int nextDayCall;
    private String templateId;
    private List<Integer> tenantBlackList; // 商户黑名单id列表
    private List<String> startWorkTimeList;
    private List<String> endWorkTimeList;
//        "taskIds": null,
    private String account;
    private String tenantCode;
    private int triggerSmsNumber;
    private int sendSmsNumber;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
