package org.example.instruction.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Getter
@Setter
public class ChangeTenantLineInstructionBean extends AbstractInstructionBean {

    private static final int WAIT_MS_FOR_CHECK = ConstUtils.WAIT_MS_FOR_CHECK;
    private static final Set<TaskStatus> TASK_STATUS_CAN_STOP_SET = StopTaskInstructionBean.TASK_STATUS_SET;
    private static final Set<TaskStatus> TASK_STATUS_CAN_ADD_TASK_SET = Sets.newHashSet(TaskStatus.UNEXCUTED, TaskStatus.ONGONIG);

    private String account;
    private String product;
    private String entranceMode;
    private TaskType taskType;
    private Set<TaskStatus> taskStatusSet;
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
    // 筛选商户线路名称
    private String filteredTenantLine;
    private String filteredCallingNumber;
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

    private Set<Long> filteredTaskIdSet;

    public ChangeTenantLineInstructionBean(
            String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, String account, String product, String entranceMode,
            TaskType taskType, List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList, List<String> taskNameSuffixList,
            String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd, String filteredTenantLine, String filteredCallingNumber, String tenantLine, String callingNumber,
            String concurrency, String expectedStartTime, String expectedEndTime, String expectedConnectedCallCount) {
        this(instructionId, instructionType, chatGroup, creator, account, product, entranceMode, taskType, taskNameEqualList,
                taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart,
                taskCreateTimeBoundEnd, filteredTenantLine, filteredCallingNumber, tenantLine, callingNumber,
                concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount, Collections.emptySet());
    }

    public ChangeTenantLineInstructionBean(
            String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, String account, String product, String entranceMode,
            TaskType taskType, List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList, List<String> taskNameSuffixList,
            String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd, String filteredTenantLine, String filteredCallingNumber, String tenantLine, String callingNumber,
            String concurrency, String expectedStartTime, String expectedEndTime, String expectedConnectedCallCount, Set<Long> filteredTaskIdSet) {
        super(instructionId, instructionType, chatGroup, creator);
        if (instructionType == InstructionType.ACTION_CHANGE_CONCURRENCY
                || instructionType == InstructionType.ACTION_ADD_TASK) {
            if (!StringUtils.isEmpty(filteredTenantLine) && StringUtils.isEmpty(tenantLine)) {
                tenantLine = filteredTenantLine;
            }
            if (StringUtils.isEmpty(filteredTenantLine) && !StringUtils.isEmpty(tenantLine)) {
                filteredTenantLine = tenantLine;
            }
            if (!StringUtils.isEmpty(filteredCallingNumber) && StringUtils.isEmpty(callingNumber)) {
                callingNumber = filteredCallingNumber;
            }
            if (StringUtils.isEmpty(filteredCallingNumber) && !StringUtils.isEmpty(callingNumber)) {
                filteredCallingNumber = callingNumber;
            }
        }
        this.account = account;
        this.product = product;
        this.entranceMode = entranceMode;
        this.taskType = taskType;
        this.taskNameEqualList = taskNameEqualList;
        this.taskNameContainList = taskNameContainList;
        this.taskNameNotContainList = taskNameNotContainList;
        this.taskNameSuffixList = taskNameSuffixList;
        this.taskCreateTimeBoundStart = taskCreateTimeBoundStart;
        this.taskCreateTimeBoundEnd = taskCreateTimeBoundEnd;
        this.filteredTenantLine = filteredTenantLine;
        this.filteredCallingNumber = filteredCallingNumber;
        this.tenantLine = tenantLine;
        this.callingNumber = callingNumber;
        this.concurrency = concurrency;
        this.expectedStartTime = expectedStartTime;
        this.expectedEndTime = expectedEndTime;
        this.expectedConnectedCallCount = expectedConnectedCallCount;
        if (instructionType == InstructionType.ACTION_ADD_TASK) {
            this.taskStatusSet = TASK_STATUS_CAN_ADD_TASK_SET;
        } else {
            this.taskStatusSet = TASK_STATUS_CAN_STOP_SET;
        }
        this.filteredTaskIdSet = filteredTaskIdSet;
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

        sb.append(this.toDetailDescription());

        return sb.toString();
    }

    public String toDetailDescription() {
        StringBuilder sb = new StringBuilder();

        sb.append("操作：").append(getInstructionType().getName().replace("操作类型_", "")).append("\n");

        String tmpAccount = account;
        if (StringUtils.isEmpty(tmpAccount)) {
            tmpAccount = "";
        }
        sb.append("账号：").append(tmpAccount).append("\n");

        String tmpProduct = product;
        if (StringUtils.isEmpty(tmpProduct)) {
            tmpProduct = "";
        }
        String tmpEntranceMode = entranceMode;
        if (StringUtils.isEmpty(tmpEntranceMode)) {
            tmpEntranceMode = "";
        }
        sb.append("业务：").append(tmpProduct).append(tmpEntranceMode).append("\n");

        String tmpTaskType = "";
        if (taskType != null) {
            tmpTaskType = taskType.getCaption();
        }
        sb.append("任务类型：").append(tmpTaskType).append("\n");

        String tmpTaskStatus = "";
        if (!CollectionUtils.isEmpty(taskStatusSet)) {
            tmpTaskStatus = taskStatusSet.stream().map(x -> x.getCaption()).collect(Collectors.joining("、"));
        }
        sb.append("任务状态：").append(tmpTaskStatus).append("\n");

        if (!StringUtils.isEmpty(filteredTenantLine) || !StringUtils.isEmpty(filteredCallingNumber)) {
            if (!StringUtils.isEmpty(filteredTenantLine)) {
                sb.append(filteredTenantLine);
            }
            if (!StringUtils.isEmpty(filteredCallingNumber)) {
                sb.append(filteredCallingNumber);
            }
            sb.append("线路的任务").append(" ");
        }

        if (!StringUtils.isEmpty(tenantLine) || !StringUtils.isEmpty(concurrency)) {
            sb.append("改用");
            if (!StringUtils.isEmpty(tenantLine) || !StringUtils.isEmpty(callingNumber)) {
                if (!StringUtils.isEmpty(tenantLine)) {
                    sb.append(tenantLine);
                }
                if (!StringUtils.isEmpty(callingNumber)) {
                    sb.append(callingNumber);
                }
                sb.append("线路").append(" ");
            }

            if (!StringUtils.isEmpty(concurrency)) {
                sb.append(concurrency).append("并发").append(" ");
            }
        }

        if (!StringUtils.isEmpty(expectedStartTime) && !StringUtils.isEmpty(expectedEndTime)) {
            sb.append(expectedStartTime).append("~").append(expectedEndTime).append(" ");
        } else if (!StringUtils.isEmpty(expectedStartTime)) {
            sb.append(expectedStartTime).append("开始").append(" ");
        } else
        if (!StringUtils.isEmpty(expectedEndTime)) {
            sb.append(expectedEndTime).append("结束").append(" ");
        }

//        if (!StringUtils.isEmpty(expectedConnectedCallCount)) {
//            sb.append("呼通到").append(expectedConnectedCallCount).append("就停").append(" ");
//        }

        if (this.getInstructionType() == InstructionType.ACTION_ADD_TASK) {
            sb.append("追加待执行任务").append(" ");
        }

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
        if (StringUtils.isEmpty(account)) {
            return new CheckResult(false,"账号为空");
        }
        if (taskType == null) {
            return new CheckResult(false,"任务类型为空");
        }
        if (this.getInstructionType() != InstructionType.ACTION_ADD_TASK) {
            if (StringUtils.isEmpty(concurrency) && StringUtils.isEmpty(expectedEndTime)) {
                return new CheckResult(false, "缺少线路参数");
            }
        }
        if (!StringUtils.isEmpty(concurrency)) {
            try {
                if (Integer.parseInt(concurrency) <= 0) {
                    return new CheckResult(false, "并发数必须为正整数");
                }
            } catch (Exception e) {
                return new CheckResult(false, "并发数必须为正整数");
            }
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                long delaySec = InstructionUtils.getDelaySec(expectedStartTime);

                BaizeClient client = BaizeClientFactory.getBaizeClient(account);

                List<AiTask> taskList = client.findAiOutboundTasks();
                taskList = InstructionUtils.filterUnlockedAiTask(taskList);
                taskList = InstructionUtils.filterAiTask(taskList, taskType);
                taskList = InstructionUtils.filterAiTask(taskList, taskStatusSet);
                taskList = InstructionUtils.filterAiTask(taskList, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd);

                if (!CollectionUtils.isEmpty(filteredTaskIdSet)) {
                    taskList = InstructionUtils.filterAiTaskById(taskList, filteredTaskIdSet);
                }

                List<AiTask> unexcutedTaskList = InstructionUtils.filterAiTask(taskList, Sets.newHashSet(TaskStatus.UNEXCUTED));
                if (this.getInstructionType() == InstructionType.ACTION_ADD_TASK
                        && CollectionUtils.isEmpty(unexcutedTaskList)) {
                    String msg = "执行指令失败！" + account + "未找到符合条件的待执行任务\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }

                taskList = InstructionUtils.filterAiTaskByTenantLineName(taskList, filteredTenantLine, filteredCallingNumber);
                if (CollectionUtils.isEmpty(taskList)) {
                    String msg = "执行指令失败！" + account + "未找到符合条件的" + taskStatusSet.stream().map(x -> x.getCaption()).collect(Collectors.joining("、")) + "任务\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }
                List<String> lackTaskNameEqualList = InstructionUtils.getLackTaskNameEqualList(taskNameEqualList, taskList);
                if (!CollectionUtils.isEmpty(lackTaskNameEqualList)) {
                    String msg = "执行指令失败！未找到符合条件的任务：\n" + String.join("\n", lackTaskNameEqualList) + "\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }
                Set<Long> filteredTaskIdSet = taskList.stream().map(task -> task.getId()).collect(Collectors.toSet());

                if (StringUtils.isEmpty(tenantLine)) {
                    Set<String> tenantLineNameSet = taskList.stream()
                            .map(task -> task.getLineName())
                            .filter(lineName -> !StringUtils.isEmpty(lineName))
                            .collect(Collectors.toSet());
                    if (tenantLineNameSet.size() > 1) {
                        String msg = "执行指令失败！" + account + "在呼商户线路多于1条：" + tenantLineNameSet + "\n" + this.toDescription();
                        Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                        return new CallableInfo(callable, 0);
                    } else if (tenantLineNameSet.size() == 1) {
                        tenantLine = tenantLineNameSet.stream().findFirst().get();
                    }
                }

                if (this.getInstructionType() == InstructionType.ACTION_ADD_TASK
                        && StringUtils.isEmpty(concurrency) && StringUtils.isEmpty(expectedEndTime)) {
                    concurrency = String.valueOf(taskList.stream().mapToInt(task -> task.getConcurrency()).sum());
                }

                String descriptionSuffix = "\n----------\n" + this.toDescription();

                List<AiTask> filteredTaskList = taskList;
                Callable<ExecutionResult> callable = () -> {
                    ExecutionResult result;
                    try {
                        if (this.getInstructionType() == InstructionType.ACTION_CHANGE_CONCURRENCY) {
                            // 处理切换线路或调整并发任务
                            List<TenantLine> curTenantLineList = getTenantLineList(client, filteredTaskList, tenantLine, callingNumber);
                            CheckResult lineCheckResult = InstructionUtils.checkTenantLineList(curTenantLineList);
                            if (lineCheckResult.isError()) {
                                String msg = "执行指令失败！" + lineCheckResult.getMsg() + "\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                            TenantLine curTenantLine = curTenantLineList.get(0);
                            int expectedConcurrency;
                            if (concurrency == null) {
                                if (!StringUtils.isEmpty(expectedEndTime)) {
                                    Date tmpEndTime = DatetimeUtils.getDatetime(expectedEndTime + ":00");
                                    tmpEndTime = DatetimeUtils.addSecond(tmpEndTime, (int) -delaySec);
                                    expectedConcurrency = client.getEstimateConcurrency(filteredTaskIdSet, tmpEndTime);
                                } else {
                                    String msg = "执行指令失败！商户线路【" + curTenantLine.getLineName() + "】所需并发为空" + "\n" + this.toDescription();
                                    return new ExecutionResult(false, msg);
                                }
                            } else {
                                expectedConcurrency = Integer.parseInt(concurrency);
                            }
                            int availableConcurrency = curTenantLine.getLineRemainConcurrent();
                            int usedConcurrency = filteredTaskList.stream()
                                    .filter(task -> Objects.equals(task.getLineCode(), curTenantLine.getLineNumber()))
                                    .mapToInt(task -> task.getConcurrency())
                                    .sum();
                            availableConcurrency += usedConcurrency;
                            if (expectedConcurrency < 1 || expectedConcurrency > availableConcurrency || expectedConcurrency < filteredTaskIdSet.size()) {
                                String msg = "执行指令失败！商户线路【" + curTenantLine.getLineName() + "】所需并发" + expectedConcurrency + "，可支配并发" + availableConcurrency + "，任务数" + filteredTaskIdSet.size() + "\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                            // TODO: 处理 expectedConnectedCallCount
                            boolean success = client.editConcurrencyAndStartTask(filteredTaskIdSet, null, curTenantLine.getId(), curTenantLine.getLineNumber(), curTenantLine.getLineName(), expectedConcurrency);
                            // 执行完成后检查是否执行成功
                            ThreadUtils.sleep(WAIT_MS_FOR_CHECK);
                            List<AiTask> finalTaskList = client.findAiOutboundTasks()
                                    .stream().filter(task -> filteredTaskIdSet.contains(task.getId())).collect(Collectors.toList());
                            finalTaskList = InstructionUtils.filterAiTask(finalTaskList, Sets.newHashSet(TaskStatus.UNEXCUTED, TaskStatus.INCOMPLETE));
                            finalTaskList = InstructionUtils.filterAiTaskByTenantLineName(finalTaskList, curTenantLine.getLineName(), null);
                            if (finalTaskList.size() > 0) {
                                success = false;
                                String note = "未开启任务数：" + finalTaskList.size() + "\n"
                                        + "未开启任务名：\n" + finalTaskList.stream().map(task -> task.getTaskName()).collect(Collectors.joining("\n"));
                                result = new ExecutionResult(success, getMsg(success, filteredTaskIdSet.size(), note));
                            } else {
                                result = new ExecutionResult(success, getMsg(success, filteredTaskIdSet.size()));
                            }
                        } else {
                            // 处理追加任务或切换线路
                            // 停止任务
        //                    List<Pair<String, String>> tenantLinePairList = Lists.newArrayList(new Pair<>(tenantLine, null));
                            SubStopTaskInstructionBean subStopTaskInstructionBean = new SubStopTaskInstructionBean(
                                    this.getInstructionId(), this.getChatGroup(), this.getCreator(), account, Lists.newArrayList(new Pair<>(product, entranceMode)),
                                    taskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd,
                                    null, null, null, descriptionSuffix, filteredTaskIdSet);
                            CallableInfo stopCallableInfo = subStopTaskInstructionBean.getCallableInfo();
                            ExecutionResult stopExecutionResult = stopCallableInfo.getCallable().call();
                            if (stopExecutionResult.isSuccess()) {
                                ThreadUtils.sleep(WAIT_MS_FOR_CHECK);
                                // 重启任务
                                SubStartTaskInstructionBean subStartTaskInstructionBean;
                                if (this.getInstructionType() == InstructionType.ACTION_ADD_TASK) {
                                    filteredTaskIdSet.addAll(unexcutedTaskList.stream().map(task -> task.getId()).collect(Collectors.toSet()));
                                    List<TaskInfoBean> taskInfoBeanList = Lists.newArrayList(new TaskInfoBean(null, null, null, null, null, null,
                                            tenantLine, callingNumber, concurrency, null, expectedEndTime, expectedConnectedCallCount));
                                    subStartTaskInstructionBean = new SubStartTaskInstructionBean(this.getInstructionId(), InstructionType.ACTION_RESTART_TASK, this.getChatGroup(), this.getCreator(), account, product, entranceMode, taskType, Sets.newHashSet(TaskStatus.INCOMPLETE, TaskStatus.UNEXCUTED), taskInfoBeanList, false, descriptionSuffix, filteredTaskIdSet);
                                } else {
                                    List<TaskInfoBean> taskInfoBeanList = Lists.newArrayList(new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd,
                                            tenantLine, callingNumber, concurrency, null, expectedEndTime, expectedConnectedCallCount));
                                    subStartTaskInstructionBean = new SubStartTaskInstructionBean(this.getInstructionId(), InstructionType.ACTION_RESTART_TASK, this.getChatGroup(), this.getCreator(), account, product, entranceMode, taskType, Sets.newHashSet(TaskStatus.INCOMPLETE), taskInfoBeanList, false, descriptionSuffix, filteredTaskIdSet);
                                }
                                result = subStartTaskInstructionBean.getCallableInfo().getCallable().call();
                                if (result.isSuccess()) {
                                    result.setMsg(result.getMsg().split("\n")[0] + "\n" + this.toDescription());
                                }
                            } else {
                                // 停止任务失败
                                result = stopExecutionResult;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                        result = new ExecutionResult(false, msg);
                    }
                    return result;
                };
                return new CallableInfo(callable, delaySec);
            } else {
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

    private static List<TenantLine> getTenantLineList(
            BaizeClient client, List<AiTask> taskList,String tenantLineName, String callingNumber) {
        AiTask task = taskList.get(0);
        TaskType taskType = task.getTaskType();
        long scriptId = task.getSpeechCraftId();
        return InstructionUtils.getTenantLineList(client, taskType, scriptId, tenantLineName, callingNumber);
    }
}
