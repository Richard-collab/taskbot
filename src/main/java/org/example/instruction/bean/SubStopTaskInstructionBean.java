package org.example.instruction.bean;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import lombok.Getter;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.AiTask;
import org.example.chat.bean.baize.TaskStatus;
import org.example.chat.bean.baize.TaskType;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.ThreadUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Getter
public class SubStopTaskInstructionBean extends StopTaskInstructionBean {

    private String account;
    private String descriptionSuffix;
    private Set<Long> filteredTaskIdSet;

    public SubStopTaskInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String account, List<Pair<String, String>> productModePairList,
            TaskType taskType, List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList,
            List<String> taskNameSuffixList, String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd, List<Pair<String, String>> tenantLinePairList, String expectedStartTime, String expectedEndTime,
            String descriptionSuffix) {
        super(instructionId, chatGroup, creator, (account == null)? null: Lists.newArrayList(account), productModePairList, taskType, taskNameEqualList, taskNameContainList,
                taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLinePairList, expectedStartTime, expectedEndTime);
        this.account = account;
        this.descriptionSuffix = descriptionSuffix;
        this.filteredTaskIdSet = Collections.emptySet();
    }

    SubStopTaskInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String account, List<Pair<String, String>> productModePairList,
            TaskType taskType, List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList,
            List<String> taskNameSuffixList, String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd,
            List<Pair<String, String>> tenantLinePairList, String expectedStartTime, String expectedEndTime,
            String descriptionSuffix, Set<Long> filteredTaskIdSet) {
        super(instructionId, chatGroup, creator, (account == null)? null: Lists.newArrayList(account), productModePairList, taskType,
                taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLinePairList, expectedStartTime, expectedEndTime);
        this.account = account;
        this.descriptionSuffix = descriptionSuffix;
        this.filteredTaskIdSet = filteredTaskIdSet;
    }

    @Override
    public String toDescription() {
        return super.toDescription() + descriptionSuffix;
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                BaizeClient client = BaizeClientFactory.getBaizeClient(account);
                List<AiTask> taskList = client.findAiOutboundTasks();
                taskList = InstructionUtils.filterUnlockedAiTask(taskList);
                taskList = InstructionUtils.filterAiTask(taskList, TASK_STATUS_SET);
                taskList = InstructionUtils.filterAiTask(taskList, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd);
                if (!CollectionUtils.isEmpty(filteredTaskIdSet)) {
                    taskList = InstructionUtils.filterAiTaskById(taskList, filteredTaskIdSet);
                }
                if (CollectionUtils.isEmpty(taskList)) {
                    String msg = "执行指令失败！" + account + "未找到符合条件的任务\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }
                if (!CollectionUtils.isEmpty(filteredTenantLinePairList)) {
                    taskList = taskList.stream()
                            .filter(task -> filteredTenantLinePairList.stream().anyMatch(pair -> InstructionUtils.isAiTask(task, pair.getKey(), pair.getValue())))
                            .collect(Collectors.toList());
                }
                if (taskType != null) {
                    taskList = InstructionUtils.filterAiTask(taskList, taskType);
                }
                if (CollectionUtils.isEmpty(taskList)) {
                    String msg = "执行指令失败！" + "未找到符合线路条件的任务\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }
                List<String> lackTaskNameEqualList = InstructionUtils.getLackTaskNameEqualList(taskNameEqualList, taskList);
                List<AiTask> stoppedTaskList = InstructionUtils.filterAiTask(taskList, ImmutableSet.of(TaskStatus.STOPPED));
                lackTaskNameEqualList = InstructionUtils.getLackTaskNameEqualList(lackTaskNameEqualList, stoppedTaskList);
                if (!CollectionUtils.isEmpty(lackTaskNameEqualList)) {
                    String msg = "执行指令失败！未找到符合条件的任务：\n" + String.join("\n", lackTaskNameEqualList) + "\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }
                Set<Long> taskIdSet = taskList.stream().map(task -> task.getId()).collect(Collectors.toSet());

                long delaySec = InstructionUtils.getDelaySec(this.getExpectedEndTime());

                Callable<ExecutionResult> callable = () -> {
                    ExecutionResult executionResult;
                    try {
                        // 开始执行
                        client.batchPreProcess(taskIdSet);
                        boolean success = client.stopTask(taskType, taskIdSet);

                        // 执行完成后检查是否执行成功
                        ThreadUtils.sleep(WAIT_MS_FOR_CHECK);
                        List<AiTask> finalTaskList = client.findAiOutboundTasks()
                                .stream().filter(task -> taskIdSet.contains(task.getId())).collect(Collectors.toList());
                        finalTaskList = InstructionUtils.filterAiTask(finalTaskList, TASK_STATUS_SET);
                        if (finalTaskList.size() > 0) {
                            success = false;
                            String note = "未停止任务数：" + finalTaskList.size()+ "\n"
                                    + "未停止任务名：\n" + finalTaskList.stream().map(task -> task.getTaskName()).collect(Collectors.joining("\n"));
                            executionResult = new ExecutionResult(success, getMsg(success, taskIdSet.size(), note));
                        } else {
                            executionResult = new ExecutionResult(success, getMsg(success, taskIdSet.size()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                        executionResult = new ExecutionResult(false, msg);
                    }
                    return executionResult;
                };
                return new CallableInfo(callable, delaySec);
            } else  {
                String msg = "执行指令失败！" + checkResult.getMsg() + "\n" + this.toDescription();
                Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                return new CallableInfo(callable, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
            Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
            return new CallableInfo(callable, 0);
        }
    }
}
