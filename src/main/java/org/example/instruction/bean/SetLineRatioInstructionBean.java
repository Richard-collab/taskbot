package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.AiTask;
import org.example.chat.bean.baize.TaskType;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.NumUtils;
import org.example.utils.StringUtils;
import org.example.utils.ThreadUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class SetLineRatioInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_SET_LINE_RATIO;
    private static final TaskType TASK_TYPE = TaskType.AI_MANUAL;

    private String account;
    private String product;
    private String entranceMode;
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
    // 筛选商户线路主叫
    private String filteredCallingNumber;
    // 集线比
    private String lineRatio;

    public SetLineRatioInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String account, String product, String entranceMode,
            List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList,
            List<String> taskNameSuffixList, String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd,
            String filteredTenantLine, String filteredCallingNumber, String lineRatio) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.account = account;
        this.product = product;
        this.entranceMode = entranceMode;
        this.taskNameEqualList = taskNameEqualList;
        this.taskNameContainList = taskNameContainList;
        this.taskNameNotContainList = taskNameNotContainList;
        this.taskNameSuffixList = taskNameSuffixList;
        this.taskCreateTimeBoundStart = taskCreateTimeBoundStart;
        this.taskCreateTimeBoundEnd = taskCreateTimeBoundEnd;
        this.filteredTenantLine = filteredTenantLine;
        this.filteredCallingNumber = filteredCallingNumber;
        this.lineRatio = lineRatio;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();

        String tmpInstructionId = this.getInstructionId();
        if (StringUtils.isEmpty(tmpInstructionId)) {
            tmpInstructionId = "";
        }
        sb.append("指令ID：").append(tmpInstructionId).append("\n");

        sb.append("群名称：").append(this.getChatGroup().getName()).append("\n");

        String tmpCreator = this.getCreator();
        if (StringUtils.isEmpty(tmpCreator)) {
            tmpCreator = "";
        }
        sb.append("创建人：").append(tmpCreator).append("\n");

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

        sb.append("任务类型：").append(TASK_TYPE.getCaption()).append("\n");

        if (!StringUtils.isEmpty(filteredTenantLine) || !StringUtils.isEmpty(filteredCallingNumber)) {
            if (!StringUtils.isEmpty(filteredTenantLine)) {
                sb.append(filteredTenantLine);
            }
            if (!StringUtils.isEmpty(filteredCallingNumber)) {
                sb.append(filteredCallingNumber);
            }
            sb.append("线路的任务").append(" ");
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
            sb.append("\n");
        }

        if (!CollectionUtils.isEmpty(taskNameEqualList)) {
            sb.append("任务列表：【\n")
                    .append(String.join("\n", taskNameEqualList))
                    .append("\n】");
            sb.append("\n");
        }

        sb.append("集线比设为").append(lineRatio);

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
//        if (TASK_TYPE == null) {
//            return new CheckResult(false,"任务类型为空");
//        }
        try {
            if (StringUtils.isEmpty(lineRatio) || Integer.parseInt(lineRatio) < 1) {
                return new CheckResult(false, "非法的集线比");
            }
        } catch (NumberFormatException e) {
            return new CheckResult(false, "非法的集线比");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                BaizeClient client = BaizeClientFactory.getBaizeClient(account);

                List<AiTask> taskList = client.findAiOutboundTasks();
                taskList = InstructionUtils.filterUnlockedAiTask(taskList);
                taskList = InstructionUtils.filterAiTask(taskList, TASK_TYPE);
                taskList = InstructionUtils.filterAiTask(taskList, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd);
                taskList = InstructionUtils.filterAiTaskByTenantLineName(taskList, filteredTenantLine, filteredCallingNumber);
                if (CollectionUtils.isEmpty(taskList)) {
                    String msg = "执行指令失败！" + account + "未找到符合条件的任务\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }
                List<String> lackTaskNameEqualList = InstructionUtils.getLackTaskNameEqualList(taskNameEqualList, taskList);
                if (!CollectionUtils.isEmpty(lackTaskNameEqualList)) {
                    String msg = "执行指令失败！未找到符合条件的任务：\n" + String.join("\n", lackTaskNameEqualList) + "\n" + this.toDescription();
                    Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                    return new CallableInfo(callable, 0);
                }
                Set<Long> taskIdSet = taskList.stream().map(task -> task.getId()).collect(Collectors.toSet());

                Callable<ExecutionResult> callable = () -> {
                    ExecutionResult result;
                    try {
                        int ratio = Integer.parseInt(lineRatio);
                        // 开始执行
                        client.batchPreProcess(taskIdSet);
                        boolean success = client.setLineRatio(taskIdSet, ratio);
                        // 执行完成后检查是否执行成功
                        ThreadUtils.sleep(WAIT_MS_FOR_CHECK);
                        List<AiTask> finalTaskList = client.findAiOutboundTasks()
                                .stream().filter(task -> taskIdSet.contains(task.getId())).collect(Collectors.toList());
                        finalTaskList = finalTaskList.stream()
                                .filter(task -> !NumUtils.equals(ratio, task.getLineRatio()))
                                .collect(Collectors.toList());
                        if (finalTaskList.size() > 0) {
                            success = false;
                            String note = "未调任务数：" + finalTaskList.size() + "\n"
                                    + "未调任务名：\n" + finalTaskList.stream().map(task -> task.getTaskName()).collect(Collectors.joining("\n"));
                            result = new ExecutionResult(success, getMsg(success, taskIdSet.size(), note));
                        } else {
                            result = new ExecutionResult(success, getMsg(success, taskIdSet.size()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                        result = new ExecutionResult(false, msg);
                    }
                    return result;
                };
                return new CallableInfo(callable, 0);
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
}
