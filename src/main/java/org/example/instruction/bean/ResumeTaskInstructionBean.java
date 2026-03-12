package org.example.instruction.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.AiTask;
import org.example.chat.bean.baize.TaskStatus;
import org.example.chat.bean.baize.TaskType;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;
import org.example.utils.ThreadUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Getter
public class ResumeTaskInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_RESUME_TASK;
    private static final Set<TaskStatus> NOT_ONGONIG_TASK_STATUS_SET = Sets.newHashSet(TaskStatus.INCOMPLETE, TaskStatus.STOPPED);

    private List<String> accountList;
    private List<TaskType> taskTypeList;
    private List<TaskStatus> taskStatusList;
    // 是否包括止损任务
    private boolean includeAutoStop;
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

    public ResumeTaskInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, List<String> accountList,
            List<TaskType> taskTypeList, List<TaskStatus> taskStatusList, Boolean includeAutoStop,
            List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList,
            List<String> taskNameSuffixList, String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (CollectionUtils.isEmpty(taskTypeList)) {
            taskTypeList = Lists.newArrayList(TaskType.AI_AUTO);
        }
        if (includeAutoStop == null) {
            includeAutoStop = true;
        }
        if (CollectionUtils.isEmpty(taskStatusList)) {
            taskStatusList = Lists.newArrayList(TaskStatus.INCOMPLETE);
        }
        this.accountList = accountList;
        this.taskTypeList = taskTypeList;
        this.taskStatusList = taskStatusList;
        this.includeAutoStop = includeAutoStop;
        this.taskNameEqualList = taskNameEqualList;
        this.taskNameContainList = taskNameContainList;
        this.taskNameNotContainList = taskNameNotContainList;
        this.taskNameSuffixList = taskNameSuffixList;
        this.taskCreateTimeBoundStart = taskCreateTimeBoundStart;
        this.taskCreateTimeBoundEnd = taskCreateTimeBoundEnd;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();

        sb.append("指令ID：");
        if (!StringUtils.isEmpty(this.getInstructionId())) {
            sb.append(this.getInstructionId());
        }
        sb.append("\n");

        sb.append("群名称：").append(this.getChatGroup().getName()).append("\n");

        sb.append("创建人：");
        if (!StringUtils.isEmpty(this.getCreator())) {
            sb.append(this.getCreator());
        }
        sb.append("\n");

        sb.append("操作：").append(getInstructionType().getName().replace("操作类型_", "")).append("\n");

        sb.append("账号：");
        if (!CollectionUtils.isEmpty(accountList)) {
            sb.append(String.join("、", accountList));
        }
        sb.append("\n");

        String tmpTaskType = "";
        if (!CollectionUtils.isEmpty(taskTypeList)) {
            tmpTaskType = taskTypeList.stream().map(taskType -> taskType.getCaption()).collect(Collectors.joining("、"));
        }
        sb.append("任务类型：").append(tmpTaskType).append("\n");

        String tmpTaskStatus = "";
        if (!CollectionUtils.isEmpty(taskTypeList)) {
            tmpTaskStatus = taskStatusList.stream().map(x -> x.getCaption()).collect(Collectors.joining("、"));
        }
        sb.append("任务状态：").append(tmpTaskStatus).append("\n");

        sb.append("包含自动停止任务：").append("是").append("\n");

        sb.append("包含止损任务：").append(includeAutoStop? "是":"否").append("\n");

        if (!CollectionUtils.isEmpty(taskNameContainList)
                || !CollectionUtils.isEmpty(taskNameNotContainList)
                || !CollectionUtils.isEmpty(taskNameSuffixList)
                || !StringUtils.isEmpty(taskCreateTimeBoundStart)
                || !StringUtils.isEmpty(taskCreateTimeBoundEnd)) {
            if (!CollectionUtils.isEmpty(taskNameContainList)) {
                sb.append("包含").append("{" + String.join("}、{", taskNameContainList) + "}").append("的任务").append(" ");
            }
            if (!CollectionUtils.isEmpty(taskNameNotContainList)) {
                sb.append("不包含").append("{" + String.join("}、{", taskNameNotContainList) + "}").append("的任务").append(" ");
            }
            if (!CollectionUtils.isEmpty(taskNameSuffixList)) {
                sb.append("以").append("{" + String.join("}、{", taskNameSuffixList) + "}").append("结尾的任务").append(" ");
            }
            if (!StringUtils.isEmpty(taskCreateTimeBoundStart) || !StringUtils.isEmpty(taskCreateTimeBoundEnd)) {
                sb.append("任务创建时间在").append(taskCreateTimeBoundStart).append("到") .append(taskCreateTimeBoundEnd).append("之间").append(" ");
            }
        }

        sb.append("\n");

        if (!CollectionUtils.isEmpty(taskNameEqualList)) {
            sb.append("任务列表：【\n")
                    .append(String.join("\n", taskNameEqualList))
                    .append("\n】");
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        if (StringUtils.isEmpty(getInstructionId())) {
            return new CheckResult(false,"指令ID为空");
        }
        if (getInstructionType() == null) {
            return new CheckResult(false,"指令类型为空");
        }
        if (CollectionUtils.isEmpty(accountList)) {
            return new CheckResult(false,"账号为空");
        }
        if (CollectionUtils.isEmpty(taskTypeList)) {
            return new CheckResult(false,"任务类型为空");
        }
        if (CollectionUtils.isEmpty(taskStatusList)) {
            return new CheckResult(false,"任务状态为空");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    try {
                        List<String> restTaskNameEqualList = new ArrayList<>(taskNameEqualList);
                        List<String> allUnresumedTaskNameList = new ArrayList<>();
                        int allResumedTaskCount = 0;
                        for (String account: accountList) {
                            BaizeClient client = BaizeClientFactory.getBaizeClient(account);
                            List<AiTask> taskList = client.findAiOutboundTasks();
                            taskList = InstructionUtils.filterUnlockedAiTask(taskList);
                            taskList = InstructionUtils.filterAiTaskByTaskType(taskList, taskTypeList);
                            taskList = InstructionUtils.filterAiTask(taskList, taskStatusList);
                            taskList = InstructionUtils.filterAiTask(taskList, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd);
                            if (!includeAutoStop) {
                                taskList = taskList.stream().filter(task -> task.getIsAutoStop() != 1).collect(Collectors.toList());
                            }
                            if (CollectionUtils.isEmpty(taskList)) {
                                String msg = "执行指令失败！" + account + "未找到符合条件的任务\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                            restTaskNameEqualList.removeAll(taskList.stream().map(task -> task.getTaskName()).collect(Collectors.toList()));

                            try {
                                // 开始执行
                                Map<TaskType, Set<Long>> taskType2taskIdSet = taskList.stream().collect(Collectors.groupingBy(x -> x.getTaskType(), Collectors.mapping(y -> y.getId(), Collectors.collectingAndThen(Collectors.toSet(), HashSet::new))));
                                for (Map.Entry<TaskType, Set<Long>> entry: taskType2taskIdSet.entrySet()) {
                                    TaskType taskType = entry.getKey();
                                    Set<Long> taskIdSet = entry.getValue();

                                    client.batchPreProcess(taskIdSet);
                                    boolean success = client.resumeTask(taskType, taskIdSet, includeAutoStop);

                                    // 执行完成后检查是否执行成功
                                    ThreadUtils.sleep(WAIT_MS_FOR_CHECK);
                                    List<AiTask> finalTaskList = client.findAiOutboundTasks()
                                            .stream().filter(task -> taskIdSet.contains(task.getId())).collect(Collectors.toList());
                                    finalTaskList = InstructionUtils.filterAiTask(finalTaskList, NOT_ONGONIG_TASK_STATUS_SET);
                                    if (finalTaskList.size() > 0) {
                                        List<String> unresumedTaskNameList = finalTaskList.stream().map(task -> task.getTaskName()).collect(Collectors.toList());
                                        allUnresumedTaskNameList.addAll(unresumedTaskNameList);
                                    } else {
                                        allResumedTaskCount += taskIdSet.size();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                        }
                        if (!CollectionUtils.isEmpty(restTaskNameEqualList) || allUnresumedTaskNameList.size() > 0) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("执行指令失败！").append("\n");
                            if (!CollectionUtils.isEmpty(restTaskNameEqualList)) {
                                sb.append("未找到符合条件的任务：\n")
                                        .append(String.join("\n", restTaskNameEqualList)).append("\n");
                            }
                            if (allUnresumedTaskNameList.size() > 0) {
                                sb.append("未恢复任务数：").append(allUnresumedTaskNameList.size()).append("\n")
                                        .append("未恢复任务名：\n").append(String.join("\n", allUnresumedTaskNameList)).append("\n");
                            }
                            sb.append(this.toDescription());

                            String msg = sb.toString();
                            return new ExecutionResult(false, msg);
                        } else {
                            return new ExecutionResult(true, getMsg(true, allResumedTaskCount));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                };
                return new CallableInfo(callable, 0);
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
