package org.example.instruction.bean;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.TaskStatus;
import org.example.chat.bean.baize.TaskType;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class StartTaskInstructionBean extends AbstractInstructionBean {

//    private static final String range = "默认";
    String account;
    private String product;
    private String entranceMode;
    TaskType taskType;
    Set<TaskStatus> taskStatusSet;
    List<TaskInfoBean> taskInfoBeanList = new ArrayList<>();
    boolean useOriginalConcurrency;

    public StartTaskInstructionBean(
            String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, String account, String product, String entranceMode,
            TaskType taskType, Set<TaskStatus> taskStatusSet, List<TaskInfoBean> taskInfoBeanList, boolean useOriginalConcurrency) {
        super(instructionId, instructionType, chatGroup, creator);
        if (!CollectionUtils.isEmpty(taskInfoBeanList)) {
            taskInfoBeanList = new ArrayList<>(new LinkedHashSet<>(taskInfoBeanList));
        }
        this.account = account;
        this.product = product;
        this.entranceMode = entranceMode;
        this.taskType = taskType;
        this.taskStatusSet = taskStatusSet;
        this.taskInfoBeanList = taskInfoBeanList;
        this.useOriginalConcurrency = useOriginalConcurrency;
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

        if (useOriginalConcurrency) {
            sb.append("沿用原并发");
        }
        if (!CollectionUtils.isEmpty(taskInfoBeanList)) {
            for (TaskInfoBean taskInfoBean : taskInfoBeanList) {
                String startTime = taskInfoBean.getExpectedStartTime();
                String endTime = taskInfoBean.getExpectedEndTime();
                String tenantLine = taskInfoBean.getTenantLine();
                String callingNumber = taskInfoBean.getCallingNumber();
                String concurrency = taskInfoBean.getConcurrency();
                String expectedConnectedCallCount = taskInfoBean.getExpectedConnectedCallCount();
                List<String> taskNameEqualList = taskInfoBean.getTaskNameEqualList();
                List<String> taskNameContainList = taskInfoBean.getTaskNameContainList();
                List<String> taskNameNotContainList = taskInfoBean.getTaskNameNotContainList();
                List<String> taskNameSuffixList = taskInfoBean.getTaskNameSuffixList();
                String taskCreateTimeBoundStart = taskInfoBean.getTaskCreateTimeBoundStart();
                String taskCreateTimeBoundEnd = taskInfoBean.getTaskCreateTimeBoundEnd();
                if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
                    sb.append(startTime).append("~").append(endTime).append(" ");
                } else if (!StringUtils.isEmpty(startTime)) {
                    sb.append(startTime).append("开始").append(" ");
                } else if (!StringUtils.isEmpty(endTime)) {
                    sb.append(endTime).append("结束").append(" ");
                }
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
//            if (!StringUtils.isEmpty(expectedConnectedCallCount)) {
//                sb.append("呼通到").append(expectedConnectedCallCount).append("就停").append(" ");
//            }
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
                            .append("\n】")
                            .append("\n");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        List<AbstractInstructionBean> instructionBeanList = new ArrayList<>();
        if (CollectionUtils.isEmpty(taskInfoBeanList) || taskInfoBeanList.size() <= 1) {
            String descriptionSuffix = "";
            SubStartTaskInstructionBean instructionBean = new SubStartTaskInstructionBean(this.getInstructionId(), this.getInstructionType(), this.getChatGroup(), this.getCreator(), account, product, entranceMode, taskType, taskStatusSet, taskInfoBeanList, useOriginalConcurrency, descriptionSuffix);
            instructionBeanList.add(instructionBean);
        } else {
            for (int i = 0; i < taskInfoBeanList.size(); i++) {
                TaskInfoBean taskInfoBean = taskInfoBeanList.get(i);
                String descriptionSuffix = "";
                if (taskInfoBeanList.size() > 1) {
                    descriptionSuffix += "\n----------\n" + this.toDescription();
                }
                SubStartTaskInstructionBean instructionBean = new SubStartTaskInstructionBean(this.getInstructionId() + "_" + i, this.getInstructionType(), this.getChatGroup(), this.getCreator(), account, product, entranceMode, taskType, taskStatusSet, Lists.newArrayList(taskInfoBean), useOriginalConcurrency, descriptionSuffix);
                instructionBeanList.add(instructionBean);
            }
        }
        return instructionBeanList;
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
        if (taskStatusSet == null
                || !(
                        (InstructionType.ACTION_START_TASK == getInstructionType() && taskStatusSet.size() == 1 && taskStatusSet.contains(TaskStatus.UNEXCUTED))
                        || (InstructionType.ACTION_RESTART_TASK == getInstructionType() &&
                                (taskStatusSet.size() == 1 && taskStatusSet.contains(TaskStatus.INCOMPLETE))
                                || (taskStatusSet.size() == 2 && taskStatusSet.contains(TaskStatus.INCOMPLETE) && taskStatusSet.contains(TaskStatus.UNEXCUTED)))
                        || (InstructionType.ACTION_RECALL_TASK == getInstructionType()
                                && (
                                        (taskStatusSet.size() == 2 && taskStatusSet.contains(TaskStatus.INCOMPLETE) && taskStatusSet.contains(TaskStatus.STOPPED))
                                                || (taskStatusSet.size() == 1 && (taskStatusSet.contains(TaskStatus.INCOMPLETE) || taskStatusSet.contains(TaskStatus.STOPPED)))
                                    )
                            )
                    )
        ) {
            return new CheckResult(false, "任务状态参数不合法");
        }
        if (taskType == TaskType.AI_AUTO) {
            // 纯ai任务
            if (useOriginalConcurrency) {
                if (this.getInstructionType() != InstructionType.ACTION_RESTART_TASK) {
                    return new CheckResult(false, "只有重启任务才支持沿用原并发");
                }
                // 沿用原并发
                if (!CollectionUtils.isEmpty(taskInfoBeanList)
                        && taskInfoBeanList.stream().anyMatch(taskInfoBean -> !taskInfoBean.checkValid(true))) {
                    return new CheckResult(false, "沿用原并发的情况下，不允许指定线路并发");
                }
            } else {
                if (CollectionUtils.isEmpty(taskInfoBeanList)) {
                    return new CheckResult(false, "缺少线路参数");
                }
                for (TaskInfoBean taskInfoBean : taskInfoBeanList) {
                    boolean flag = taskInfoBean.checkValid(false);
                    if (!flag) {
                        return new CheckResult(false, "线路参数不合法");
                    }
                }
            }
        }
        return new CheckResult(true, "");
    }

    public CallableInfo getCallableInfo() {
        return null;
    }

}
