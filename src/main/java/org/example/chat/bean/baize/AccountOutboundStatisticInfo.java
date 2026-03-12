package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
@Setter
public class AccountOutboundStatisticInfo implements Serializable {

    private String account;
    private String name;
    private String groupId;
    private String tenantId;
    private String tenantNo;
    private String tenantName;
    private int executingTasksNum;
    private int executedTasksNum;
    private int waitingTasksNum;
    private int totalTasksNum;
    private int calledPhoneNum; // 已呼叫名单数
    private int planedPhoneNum; // 导入名单数
    private int outboundCalledNum; // 已呼量
    private int outboundTotalNum; // 呼叫总量
    private int firstCallRemainNum; // 首呼剩余名单数
    private int recalledRemainNum; // 补呼剩余名单数
    private int averageCallDurations; // 平均通时
    private int intentionNums;
    private int classANum;
    private float classARatio;
    private int classBNum;
    private float classBRatio;
    private int classCNum;
    private float classCRatio;
    private int classDNum;
    private float classDRatio;
    private int calledNum;
    private int putThroughNum; // 接通电话数

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
