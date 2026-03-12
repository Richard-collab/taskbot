package org.example.instruction.bean;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.ForbiddenDistrictUtils;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class SubStartTaskInstructionBean extends StartTaskInstructionBean {

    private static final int TH_MAX_FINISHED_TOTAL_PHONE_COUNT = 50 * 10000;
    private static final int TH_MAX_FINISHED_TASK_PHONE_COUNT = 10 * 10000;
    private static final int TH_MAX_FINISHED_TASK_COUNT = 5;

    private static final Set<TaskStatus> ONGOING_TASK_STATUS_SET = Sets.newHashSet(TaskStatus.ONGONIG);

    private String descriptionSuffix;
    private Set<Long> filteredTaskIdSet;

    public SubStartTaskInstructionBean(String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, String account, String product, String entranceMode, TaskType taskType, Set<TaskStatus> taskStatusList, List<TaskInfoBean> taskInfoBeanList, boolean originalLineAndConcurrency, String descriptionSuffix) {
        super(instructionId, instructionType, chatGroup, creator, account, product, entranceMode, taskType, taskStatusList, taskInfoBeanList, originalLineAndConcurrency);
        this.descriptionSuffix = descriptionSuffix;
        this.filteredTaskIdSet = Collections.emptySet();
    }

    SubStartTaskInstructionBean(String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, String account, String product, String entranceMode, TaskType taskType, Set<TaskStatus> taskStatusList, List<TaskInfoBean> taskInfoBeanList, boolean originalLineAndConcurrency, String descriptionSuffix, Set<Long> filteredTaskIdSet) {
        super(instructionId, instructionType, chatGroup, creator, account, product, entranceMode, taskType, taskStatusList, taskInfoBeanList, originalLineAndConcurrency);
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
//                String groupId = client.getGroupId();
                List<AiTask> allTaskList = client.findAiOutboundTasks();
                allTaskList = InstructionUtils.filterUnlockedAiTask(allTaskList);
                List<AiTask> taskList = new ArrayList<>(allTaskList);
                taskList = InstructionUtils.filterAiTask(taskList, taskType);
                taskList = InstructionUtils.filterAiTask(taskList, taskStatusSet);

                TaskInfoBean taskInfoBean = null;
                String strExpectedConcurrency;
                String strExpectedStartTime = null;
                String strExpectedEndTime;
                List<String> taskNameEqualList = null;
                String equalStopMsg = "";
                if (!CollectionUtils.isEmpty(taskInfoBeanList)) {
                    taskInfoBean = taskInfoBeanList.get(0);
                    strExpectedConcurrency = taskInfoBean.getConcurrency();
                    strExpectedStartTime = taskInfoBean.getExpectedStartTime();
                    strExpectedEndTime = taskInfoBean.getExpectedEndTime();

                    List<String> taskNameContainList = taskInfoBean.getTaskNameContainList();
                    List<String> taskNameNotContainList = taskInfoBean.getTaskNameNotContainList();
                    List<String> taskNameSuffixList = taskInfoBean.getTaskNameSuffixList();
                    String taskCreateTimeBoundStart = taskInfoBean.getTaskCreateTimeBoundStart();
                    String taskCreateTimeBoundEnd = taskInfoBean.getTaskCreateTimeBoundEnd();
                    taskNameEqualList = taskInfoBean.getTaskNameEqualList();

                    // 复呼任务，对于指定的任务，如果是进行中，则先暂停
                    if (this.getInstructionType() == InstructionType.ACTION_RECALL_TASK
                            && !CollectionUtils.isEmpty(taskNameEqualList)) {
                        List<AiTask> equalTaskList = new ArrayList<>(allTaskList);
                        equalTaskList = InstructionUtils.filterAiTask(equalTaskList, taskType);
                        equalTaskList = InstructionUtils.filterAiTask(equalTaskList, ONGOING_TASK_STATUS_SET);
                        equalTaskList = InstructionUtils.filterAiTask(equalTaskList, taskNameEqualList, null, null, null, null, null);
                        if (!CollectionUtils.isEmpty(equalTaskList)) {
                            Set<Long> filteredEqualTaskIdSet = equalTaskList.stream().map(task -> task.getId()).collect(Collectors.toSet());
                            SubStopTaskInstructionBean stopTaskInstructionBean = new SubStopTaskInstructionBean(this.getInstructionId(), this.getChatGroup(), this.getCreator(), account,
                                    Lists.newArrayList(new Pair<>(this.getProduct(), this.getEntranceMode())), taskType, null, null, null, null, null, null, null, null, null,
                                    descriptionSuffix, filteredEqualTaskIdSet);
                            ExecutionResult stopExecutionResult = stopTaskInstructionBean.getCallableInfo().getCallable().call();
                            if (!stopExecutionResult.isSuccess()) {
                                String msg = stopExecutionResult.getMsg() + "\n" + this.toDescription();
                                Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                                return new CallableInfo(callable, 0);
                            }
                            equalStopMsg = "请注意，以下【进行中】的任务也已执行复呼：\n" + equalTaskList.stream().map(task -> "• " + task.getTaskName() + "（" + NumUtils.roundStringFormat(100.0 * task.getFinishedPhoneNum() / task.getPhoneNum(), 2) + "% " + task.getFinishedPhoneNum() + "/" + task.getPhoneNum() + "）").collect(Collectors.joining("\n")) + "\n";

                            allTaskList = client.findAiOutboundTasks();
                            allTaskList = InstructionUtils.filterUnlockedAiTask(allTaskList);
                            taskList = new ArrayList<>(allTaskList);
                            taskList = InstructionUtils.filterAiTask(taskList, taskType);
                            taskList = InstructionUtils.filterAiTask(taskList, taskStatusSet);
                        }
                    }

                    taskList = InstructionUtils.filterAiTask(taskList, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd);
                } else {
                    strExpectedEndTime = null;
                    strExpectedConcurrency = null;
                }

                if (!CollectionUtils.isEmpty(filteredTaskIdSet)) {
                    taskList = InstructionUtils.filterAiTaskById(taskList, filteredTaskIdSet);
                }

                if (CollectionUtils.isEmpty(taskList)) {
                    String msg = "执行指令失败！未找到符合条件的任务\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }
                List<String> lackTaskNameEqualList = InstructionUtils.getLackTaskNameEqualList(taskNameEqualList, taskList);
                if (getInstructionType() == InstructionType.ACTION_RESTART_TASK) {
                    List<AiTask> stoppedTaskList = InstructionUtils.filterAiTask(taskList, ImmutableSet.of(TaskStatus.STOPPED));
                    lackTaskNameEqualList = InstructionUtils.getLackTaskNameEqualList(lackTaskNameEqualList, stoppedTaskList);
                }
                if (!CollectionUtils.isEmpty(lackTaskNameEqualList)) {
                    String msg = "执行指令失败！未找到符合条件的任务：\n" + String.join("\n", lackTaskNameEqualList) + "\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }

                Long scriptId = taskList.stream().map(task -> task.getSpeechCraftId()).findFirst().orElse(null);
                List<TenantLine> tenantLineList = getTenantLineList(client, taskType, scriptId, taskInfoBean);
                // 重启任务时没指定线路的，沿用原来的线路
                if (this.getInstructionType() == InstructionType.ACTION_RESTART_TASK
                        && tenantLineList.size() > 1
                        && (taskInfoBean == null || (StringUtils.isEmpty(taskInfoBean.getTenantLine()) && StringUtils.isEmpty(taskInfoBean.getCallingNumber())))) {
                    Map<String, List<AiTask>> tenantLineName2taskList = taskList.stream().collect(
                            Collectors.groupingBy(task -> task.getLineName(), LinkedHashMap::new, Collectors.toList()));
                    if (tenantLineName2taskList.size() == 1) {
                        String tenantLineName = tenantLineName2taskList.keySet().stream().findFirst().get();
                        tenantLineList = InstructionUtils.filterTenantLine(tenantLineList, tenantLineName, null);
                    } else {
                        String msg = "执行指令失败！未指定商户线路，且任务使用了多条商户线路：" +  String.join("、", tenantLineName2taskList.keySet()) + "\n" + this.toDescription();
                        Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                        return new CallableInfo(callable, 0);
                    }
                }

                // 如果是沿用原并发
                if (this.isUseOriginalConcurrency()) {
                    List<String> illegalTaskNameList = taskList.stream()
                            .filter(task -> task.getConcurrency() <= 0)
                            .map(task -> task.getTaskName()).collect(Collectors.toList());
                    if (!illegalTaskNameList.isEmpty()) {
                        String msg = "执行指令失败！有任务未指定并发：" +  String.join("、", illegalTaskNameList) + "\n" + this.toDescription();
                        Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                        return new CallableInfo(callable, 0);
                    }
                    int expectedConcurrency = taskList.stream().mapToInt(task -> task.getConcurrency()).sum();
                    strExpectedConcurrency = String.valueOf(expectedConcurrency);
                }

                CheckResult lineCheckResult = InstructionUtils.checkTenantLineList(tenantLineList);
                if (lineCheckResult.isError()) {
                    String msg = "执行指令失败！" + lineCheckResult.getMsg() + "\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }

                long delaySec = InstructionUtils.getDelaySec(strExpectedStartTime);

                TaskInfoBean curTaskInfoBean = taskInfoBean;
                List<AiTask> curTaskList = taskList;
                String finalStrExpectedConcurrency = strExpectedConcurrency;
                List<TenantLine> finalTenantLineList = tenantLineList;
                String finalEqualStopMsg = equalStopMsg;
                Callable<ExecutionResult> callable = () -> {
                    try {
                        ExecutionResult executionResult;

                        Set<Long> taskIdSet = curTaskList.stream().map(task -> task.getId()).collect(Collectors.toSet());

                        // 如果有全天屏蔽地区的，先屏蔽地区
                        if (this.getInstructionType() == InstructionType.ACTION_START_TASK
                                || this.getInstructionType() == InstructionType.ACTION_RESTART_TASK) {
                            DistrictBeanInfo districtBeanInfo = ForbiddenDistrictUtils.get(account);
                            if (districtBeanInfo != null
                                    && !(CollectionUtils.isEmpty(districtBeanInfo.getForbiddenProvinceSet()) && CollectionUtils.isEmpty(districtBeanInfo.getForbiddenCitySet()))) {
                                ForbidDistrictInstructionBean forbidDistrictInstructionBean = new ForbidDistrictInstructionBean(
                                        this.getInstructionId() + "_1", InstructionType.ACTION_FORBID_DISTRICT, this.getChatGroup(),
                                        this.getCreator(), account, null, null, null, null, null, null, null,
                                        null, districtBeanInfo.getForbiddenProvinceSet(), districtBeanInfo.getForbiddenCitySet(), false, taskIdSet);
                                ExecutionResult forbidExecutionResult = forbidDistrictInstructionBean.getCallableInfo().getCallable().call();
                                if (!forbidExecutionResult.isSuccess()) {
                                    String msg = "屏蔽地区时" + forbidExecutionResult.getMsg() + "\n" + forbidDistrictInstructionBean.toDescription() + "\n----------\n" + this.toDescription();
                                    return new ExecutionResult(false, msg);
                                }
                            }
                        }

                        // 开始执行任务
                        List<TenantLine> curTenantLineList = getTenantLineList(client, taskType, scriptId, curTaskInfoBean);
                        curTenantLineList = curTenantLineList.stream()
                                .filter(x -> finalTenantLineList.stream().anyMatch(y -> Objects.equals(y.getLineNumber(), x.getLineNumber())))
                                .collect(Collectors.toList());

                        CheckResult curLineCheckResult = InstructionUtils.checkTenantLineList(curTenantLineList);
                        if (curLineCheckResult.isError()) {
                            String msg = "执行指令失败！" + curLineCheckResult.getMsg() + "\n" + this.toDescription();
                            executionResult = new ExecutionResult(false, msg);
                        } else {
                            TenantLine curTenantLine = curTenantLineList.get(0);

                            int availableConcurrency = curTenantLine.getLineRemainConcurrent();
                            InstructionType instructionType = getInstructionType();
                            switch (instructionType) {
                                case ACTION_RECALL_TASK:
                                case ACTION_START_TASK:
                                case ACTION_RESTART_TASK: {
                                    if (instructionType == InstructionType.ACTION_RECALL_TASK) {
                                        // 复呼任务需要先批量添加呼叫
//                                        client.batchPreProcess(taskIdSet); // 复呼不需要preprocess
                                        int totalPhoneCount = client.findFinishedPhoneCount(taskType, taskIdSet);
                                        if (totalPhoneCount > 0) {
//                                            System.out.println(DatetimeUtils.getStrDatetime(new Date()) + " " + this.getInstructionId() + " 开始批量添加呼叫");

                                            List<List<AiTask>> taskListList = new ArrayList<>();
                                            int tmpPhoneCount = 0;
                                            List<AiTask> tmpTaskList = new ArrayList<>();
                                            for (AiTask task : curTaskList) {
                                                int phoneCount = task.getPhoneNum();
                                                if (tmpTaskList.size() < TH_MAX_FINISHED_TASK_COUNT
                                                        && phoneCount <= TH_MAX_FINISHED_TASK_PHONE_COUNT
                                                        && tmpPhoneCount + phoneCount <= TH_MAX_FINISHED_TOTAL_PHONE_COUNT) {
                                                    tmpTaskList.add(task);
                                                    tmpPhoneCount += phoneCount;
                                                } else {
                                                    taskListList.add(tmpTaskList);
                                                    tmpPhoneCount = 0;
                                                    tmpTaskList = new ArrayList<>();
                                                }
                                            }
                                            if (!CollectionUtils.isEmpty(tmpTaskList)) {
                                                taskListList.add(tmpTaskList);
                                            }

                                            boolean isSuccess = true;
                                            for (List<AiTask> tempTaskList: taskListList) {
                                                Set<Long> tempTaskIdSet = tempTaskList.stream().map(task -> task.getId()).collect(Collectors.toSet());
                                                boolean success = client.addFinishedPhoneList(taskType, tempTaskIdSet);
                                                if (!success) {
                                                    isSuccess = false;
                                                }
                                            }

//                                            System.out.println(DatetimeUtils.getStrDatetime(new Date()) + " " + this.getInstructionId() + " 批量添加呼叫完成，添加成功？" + isSuccess);

                                            if (!isSuccess) {
                                                String msg = "执行指令失败！无法批量添加呼叫\n" + this.toDescription();
                                                executionResult = new ExecutionResult(false, msg);
                                                break;
                                            }

                                        } else if (totalPhoneCount == 0) {
                                            String msg = "执行指令失败！共有0条数据满足筛选条件\n" + this.toDescription();
                                            executionResult = new ExecutionResult(false, msg);
                                            break;
                                        } else {
                                            String msg = "执行指令失败！无法批量添加呼叫\n" + this.toDescription();
                                            executionResult = new ExecutionResult(false, msg);
                                            break;
                                        }
                                    }

                                    // 开始呼叫
                                    boolean success;
                                    if (taskType == TaskType.AI_MANUAL) {
                                        // 人机协同
                                        client.batchPreProcess(taskIdSet);

                                        boolean isIncludeAutoStop = (instructionType == InstructionType.ACTION_RESTART_TASK);
                                        success = client.startAiManualTask(taskIdSet, curTenantLine, isIncludeAutoStop);
                                    } else {
                                        // 纯ai
                                        if (!StringUtils.isEmpty(finalStrExpectedConcurrency)) {
                                            int expectedConcurrency = Integer.parseInt(finalStrExpectedConcurrency);
                                            if (expectedConcurrency < 1 || expectedConcurrency > availableConcurrency || expectedConcurrency < taskIdSet.size()) {
                                                String msg = "执行指令失败！商户线路【" + curTenantLine.getLineName() + "】所需并发" + expectedConcurrency + "，可支配并发" + availableConcurrency + "，任务数" + taskIdSet.size() + "\n" + this.toDescription();
                                                executionResult = new ExecutionResult(false, msg);
                                                break;
                                            } else {
                                                client.batchPreProcess(taskIdSet);

                                                boolean isIncludeAutoStop = (instructionType == InstructionType.ACTION_RESTART_TASK);
                                                success = client.startAiAutoTask(taskIdSet, curTenantLine, expectedConcurrency, isIncludeAutoStop);
                                            }
                                        } else {
                                            Date expectedEndTime = DatetimeUtils.getDatetime(strExpectedEndTime + ":00");
                                            Date tmpEndTime = DatetimeUtils.addSecond(expectedEndTime, (int) -delaySec);
                                            int expectedConcurrency = client.getEstimateConcurrency(taskIdSet, tmpEndTime);
                                            if (expectedConcurrency < 1 || expectedConcurrency > availableConcurrency || expectedConcurrency < taskIdSet.size()) {
                                                String msg = "执行指令失败！商户线路【" + curTenantLine.getLineName() + "】所需并发" + expectedConcurrency + "，可支配并发" + availableConcurrency + "，任务数" + taskIdSet.size() + "\n" + this.toDescription();
                                                executionResult = new ExecutionResult(false, msg);
                                                break;
                                            } else {
                                                client.batchPreProcess(taskIdSet);

                                                boolean isIncludeAutoStop = (instructionType == InstructionType.ACTION_RESTART_TASK);
                                                success = client.startAiAutoTask(taskIdSet, curTenantLine, expectedEndTime, isIncludeAutoStop);
                                            }
                                        }
                                    }
                                    // 执行完成后检查是否执行成功
                                    ThreadUtils.sleep(WAIT_MS_FOR_CHECK);
                                    List<AiTask> finalTaskList = client.findAiOutboundTasks()
                                            .stream().filter(task -> taskIdSet.contains(task.getId())).collect(Collectors.toList());
                                    finalTaskList = InstructionUtils.filterAiTask(finalTaskList, Sets.newHashSet(TaskStatus.UNEXCUTED, TaskStatus.INCOMPLETE));
                                    if (finalTaskList.size() > 0) {
                                        success = false;
                                        String note = "未开启任务数：" + finalTaskList.size() + "\n"
                                                + "未开启任务名：\n" + finalTaskList.stream().map(task -> task.getTaskName()).collect(Collectors.joining("\n")) + "\n"
                                                + finalEqualStopMsg;
                                        executionResult = new ExecutionResult(success, getMsg(success, taskIdSet.size(), note));
                                    } else {
                                        executionResult = new ExecutionResult(success, getMsg(success, taskIdSet.size(), finalEqualStopMsg));
                                    }
                                    break;
                                }
                                default: {
                                    String msg = "执行指令失败！不支持该类型任务\n" + this.toDescription();
                                    executionResult = new ExecutionResult(false, msg);
                                    break;
                                }
                            }
                        }
                        return executionResult;
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
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
            BaizeClient client, TaskType taskType, Long scriptId, TaskInfoBean taskInfoBean) {
        String tenantLine = null;
        String callingNumber = null;
        if (taskInfoBean != null) {
            tenantLine = taskInfoBean.getTenantLine();
            callingNumber = taskInfoBean.getCallingNumber();
        }
        return InstructionUtils.getTenantLineList(client, taskType, scriptId, tenantLine, callingNumber);
    }
}
