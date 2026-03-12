package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
public class LineTaskInfo implements Serializable {

    private static final int TH_ACCOUNT_REST_PHONE_COUNT = 10000; // 账号首呼剩余+补呼剩余 低于阈值的，不重新分配并发
    private static final float TH_DELTA_CONCURRENCY = 500; // 账号前后并发数相差低于阈值的，不重新分配并发
    private static final float TH_DELTA_CONCURRENCY_RATIO = 1.1f; // 账号前后并发数百分比低于阈值的，不重新分配并发

    private String account;
    private Integer lineId;
    private String lineCode;
    private String lineName;
    private List<AiTask> taskList;
    @Setter
    private Integer requiredConcurrencyByPhone;
    @Setter
    private Integer requiredConcurrencyByLock;

    private int restPhoneCount;
    private int lockedConcurrency;

    public LineTaskInfo(
            String account, Integer lineId, String lineCode, String lineName, List<AiTask> taskList,
            Integer requiredConcurrencyByLock, Integer requiredConcurrencyByPhone) {
        this.account = account;
        this.lineId = lineId;
        this.lineCode = lineCode;
        this.lineName = lineName;
        this.taskList = taskList;
        this.requiredConcurrencyByLock = requiredConcurrencyByLock;
        this.requiredConcurrencyByPhone = requiredConcurrencyByPhone;
        this.restPhoneCount = taskList.stream().mapToInt(task -> task.getCallingPhoneNum() + task.getRecallingPhoneNum()).sum();
        this.lockedConcurrency = taskList.stream().mapToInt(task -> task.getConcurrency()).sum();
    }

    public int getTaskCount() {
        return taskList.size();
    }

    /**
     * 锁定并发是否远超所需
     *
     * @return
     */
    public boolean isConcurrencyOverkill() {
        // 任务锁定并发比应锁超过10%且超锁并发大于500，且剩余名单数超过1万，进行预警
        int lockedConcurrency = getLockedConcurrency();
        int restPhoneCount = getRestPhoneCount();
        return requiredConcurrencyByLock != null
                && lockedConcurrency - requiredConcurrencyByLock > TH_DELTA_CONCURRENCY
                && lockedConcurrency / (float) requiredConcurrencyByLock > TH_DELTA_CONCURRENCY_RATIO
                && restPhoneCount > TH_ACCOUNT_REST_PHONE_COUNT;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
