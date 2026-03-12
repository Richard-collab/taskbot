package org.example.instruction.bean;

import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class TaskInfoBean implements Serializable {

    // 任务名称
    private List<String> taskNameEqualList;
    // 任务名称包含
    private List<String> taskNameContainList;
    // 任务名称不包含
    private List<String> taskNameNotContainList;
    // 任务名称后缀
    private List<String> taskNameSuffixList;
    // 任务创建时间上限
    private String taskCreateTimeBoundStart;
    // 任务创建时间下限
    private String taskCreateTimeBoundEnd;
    // 商户线路名称
    private String tenantLine;
    private String callingNumber;
    // 并发数
    private String concurrency;
    // 预期开始时间
    private String expectedStartTime = null;
    // 预期完成时间
    private String expectedEndTime = null;
    // 预期呼通数
    private String expectedConnectedCallCount = null;

    public TaskInfoBean(
            List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList,
            List<String> taskNameSuffixList, String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd,
            String tenantLine, String callingNumber, String concurrency, String expectedStartTime, String expectedEndTime,
            String expectedConnectedCallCount) {
        if (!CollectionUtils.isEmpty(taskNameEqualList)) {
            taskNameEqualList = new ArrayList<>(new LinkedHashSet<>(taskNameEqualList));
        }
        if (!CollectionUtils.isEmpty(taskNameContainList)) {
            taskNameContainList = new ArrayList<>(new LinkedHashSet<>(taskNameContainList));
        }
        if (!CollectionUtils.isEmpty(taskNameNotContainList)) {
            taskNameNotContainList = new ArrayList<>(new LinkedHashSet<>(taskNameNotContainList));
        }
        if (!CollectionUtils.isEmpty(taskNameSuffixList)) {
            taskNameSuffixList = new ArrayList<>(new LinkedHashSet<>(taskNameSuffixList));
        }
        this.taskNameEqualList = taskNameEqualList;
        this.taskNameContainList = taskNameContainList;
        this.taskNameNotContainList = taskNameNotContainList;
        this.taskNameSuffixList = taskNameSuffixList;
        this.taskCreateTimeBoundStart = taskCreateTimeBoundStart;
        this.taskCreateTimeBoundEnd = taskCreateTimeBoundEnd;
        this.tenantLine = tenantLine;
        this.callingNumber = callingNumber;
        this.concurrency = concurrency;
        this.expectedStartTime = expectedStartTime;
        this.expectedEndTime = expectedEndTime;
        this.expectedConnectedCallCount = expectedConnectedCallCount;
    }

    public List<String> getTaskNameEqualList() {
        return taskNameEqualList;
    }

    public void setTaskNameEqualList(List<String> taskNameEqualList) {
        this.taskNameEqualList = taskNameEqualList;
    }

    public List<String> getTaskNameContainList() {
        return taskNameContainList;
    }

    public void setTaskNameContainList(List<String> taskNameContainList) {
        this.taskNameContainList = taskNameContainList;
    }

    public List<String> getTaskNameNotContainList() {
        return taskNameNotContainList;
    }

    public void setTaskNameNotContainList(List<String> taskNameNotContainList) {
        this.taskNameNotContainList = taskNameNotContainList;
    }

    public List<String> getTaskNameSuffixList() {
        return taskNameSuffixList;
    }

    public void setTaskNameSuffixList(List<String> taskNameSuffixList) {
        this.taskNameSuffixList = taskNameSuffixList;
    }

    public String getTaskCreateTimeBoundStart() {
        return taskCreateTimeBoundStart;
    }

    public void setTaskCreateTimeBoundStart(String taskCreateTimeBoundStart) {
        this.taskCreateTimeBoundStart = taskCreateTimeBoundStart;
    }

    public String getTaskCreateTimeBoundEnd() {
        return taskCreateTimeBoundEnd;
    }

    public void setTaskCreateTimeBoundEnd(String taskCreateTimeBoundEnd) {
        this.taskCreateTimeBoundEnd = taskCreateTimeBoundEnd;
    }

    public String getTenantLine() {
        return tenantLine;
    }

    public String getCallingNumber() {
        return callingNumber;
    }

    public String getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }

    public String getExpectedStartTime() {
        return expectedStartTime;
    }

    public void setExpectedStartTime(String expectedStartTime) {
        this.expectedStartTime = expectedStartTime;
    }

    public String getExpectedEndTime() {
        return expectedEndTime;
    }

    public void setExpectedEndTime(String expectedEndTime) {
        this.expectedEndTime = expectedEndTime;
    }

    public String getExpectedConnectedCallCount() {
        return expectedConnectedCallCount;
    }

    public void setExpectedConnectedCallCount(String expectedConnectedCallCount) {
        this.expectedConnectedCallCount = expectedConnectedCallCount;
    }

    public boolean checkValid(boolean useOriginalConcurrency) {
        try {
            // 该账户下商户线路只有1条的，可以不指定商户线路
            // 重启任务时没指定线路的，沿用原来的线路
            if (useOriginalConcurrency) {
                // 沿用原并发
                return StringUtils.isEmpty(concurrency) && StringUtils.isEmpty(expectedEndTime);
            } else {
                // 不是原并发
//            return (!StringUtils.isEmpty(tenantLine) || !StringUtils.isEmpty(callingNumber))
                return (!StringUtils.isEmpty(concurrency) || !StringUtils.isEmpty(expectedEndTime))
                        && !(!StringUtils.isEmpty(concurrency) && !StringUtils.isEmpty(expectedEndTime))
                        && (StringUtils.isEmpty(concurrency) || Integer.parseInt(concurrency) > 0);
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof TaskInfoBean)) {
            return false;
        }
        TaskInfoBean taskInfoBean = (TaskInfoBean) o;
        return CollectionUtils.isEqual(taskNameEqualList, taskInfoBean.taskNameEqualList)
                && CollectionUtils.isEqual(taskNameContainList, taskInfoBean.taskNameContainList)
                && CollectionUtils.isEqual(taskNameNotContainList, taskInfoBean.taskNameNotContainList)
                && CollectionUtils.isEqual(taskNameSuffixList, taskInfoBean.taskNameSuffixList)
                && Objects.equals(this.taskCreateTimeBoundStart, taskInfoBean.taskCreateTimeBoundStart)
                && Objects.equals(this.taskCreateTimeBoundEnd, taskInfoBean.taskCreateTimeBoundEnd)
                && Objects.equals(this.tenantLine, taskInfoBean.tenantLine)
                && Objects.equals(this.callingNumber, taskInfoBean.callingNumber)
                && Objects.equals(this.concurrency, taskInfoBean.concurrency)
                && Objects.equals(this.expectedStartTime, taskInfoBean.expectedStartTime)
                && Objects.equals(this.expectedEndTime, taskInfoBean.expectedEndTime)
                && Objects.equals(this.expectedConnectedCallCount, taskInfoBean.expectedConnectedCallCount);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.taskNameEqualList == null? 0: this.taskNameEqualList.hashCode());
        result = 37 * result + (this.taskNameContainList == null? 0: this.taskNameContainList.hashCode());
        result = 37 * result + (this.taskNameNotContainList == null? 0: this.taskNameNotContainList.hashCode());
        result = 37 * result + (this.taskNameSuffixList == null? 0: this.taskNameSuffixList.hashCode());
        result = 37 * result + (this.taskCreateTimeBoundStart == null? 0: this.taskCreateTimeBoundStart.hashCode());
        result = 37 * result + (this.taskCreateTimeBoundEnd == null? 0: this.taskCreateTimeBoundEnd.hashCode());
        result = 37 * result + (this.tenantLine == null? 0: this.tenantLine.hashCode());
        result = 37 * result + (this.callingNumber == null? 0: this.callingNumber.hashCode());
        result = 37 * result + (this.concurrency == null? 0: this.concurrency.hashCode());
        result = 37 * result + (this.expectedStartTime == null? 0: this.expectedStartTime.hashCode());
        result = 37 * result + (this.expectedEndTime == null? 0: this.expectedEndTime.hashCode());
        result = 37 * result + (this.expectedConnectedCallCount == null? 0: this.expectedConnectedCallCount.hashCode());
        return result;
    }
}
