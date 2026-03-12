package org.example.instruction.bean;

import com.google.common.collect.ImmutableSet;
import javafx.util.Pair;
import lombok.Getter;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.TaskStatus;
import org.example.chat.bean.baize.TaskType;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class StopTaskInstructionBean extends AbstractInstructionBean {

    static final Set<TaskStatus> TASK_STATUS_SET = ImmutableSet.of(TaskStatus.ONGONIG);
    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_STOP_TASK;

    private List<String> accountList;
    List<Pair<String, String>> productModePairList;
    TaskType taskType;
    // 任务名称
    List<String> taskNameEqualList;
    // 任务名称包含
    List<String> taskNameContainList;
    // 任务名称不包含
    List<String> taskNameNotContainList;
    // 任务名称后缀
    List<String> taskNameSuffixList;
    // 任务创建时间上限
    String taskCreateTimeBoundStart;
    // 任务创建时间下限
    String taskCreateTimeBoundEnd;
    // 商户线路、主叫号码对
    List<Pair<String,String>> filteredTenantLinePairList;
    // 预期开始时间，到时候再开呼
    private String expectedStartTime;
    // 预期完成时间，先到点停呼
    private String expectedEndTime;

    public StopTaskInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, List<String> accountList, List<Pair<String, String>> productModePairList,
            TaskType taskType, List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList,
            List<String> taskNameSuffixList, String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd,
            List<Pair<String, String>> filteredTenantLinePairList, String expectedStartTime, String expectedEndTime) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (!CollectionUtils.isEmpty(accountList)) {
            accountList = accountList.stream()
                    .filter(account -> !StringUtils.isEmpty(account))
                    .distinct()
                    .collect(Collectors.toList());
        }
        this.accountList = accountList;
        this.productModePairList = productModePairList;
        this.taskType = taskType;
        this.taskNameEqualList = taskNameEqualList;
        this.taskNameContainList = taskNameContainList;
        this.taskNameNotContainList = taskNameNotContainList;
        this.taskNameSuffixList = taskNameSuffixList;
        this.taskCreateTimeBoundStart = taskCreateTimeBoundStart;
        this.taskCreateTimeBoundEnd = taskCreateTimeBoundEnd;
        this.filteredTenantLinePairList = filteredTenantLinePairList;
        this.expectedStartTime = expectedStartTime;
        this.expectedEndTime = expectedEndTime;
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

        sb.append("业务：");
        if (!CollectionUtils.isEmpty(productModePairList)) {
            sb.append(productModePairList.stream().map(pair -> {
                String tmpProduct = pair.getKey();
                if (StringUtils.isEmpty(tmpProduct)) {
                    tmpProduct = "";
                }
                String tmpEntranceMode = pair.getValue();
                if (StringUtils.isEmpty(tmpEntranceMode)) {
                    tmpEntranceMode = "";
                }
                return tmpProduct + tmpEntranceMode;
            }).collect(Collectors.joining("、")));
        }
        sb.append("\n");

        String tmpTaskType = "";
        if (taskType != null) {
            tmpTaskType = taskType.getCaption();
        }
        sb.append("任务类型：").append(tmpTaskType).append("\n");

        String tmpTaskStatus = "";
        if (!CollectionUtils.isEmpty(TASK_STATUS_SET)) {
            tmpTaskStatus = TASK_STATUS_SET.stream().map(x -> x.getCaption()).collect(Collectors.joining("、"));
        }
        sb.append("任务状态：").append(tmpTaskStatus).append("\n");

        if (!CollectionUtils.isEmpty(filteredTenantLinePairList)) {
            sb.append("针对").append(filteredTenantLinePairList.stream().map(pair -> (pair.getKey() == null? "": pair.getKey()) + (pair.getValue() == null? "": pair.getValue())).collect(Collectors.joining("、"))).append("线路").append(" ");
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

        if (!StringUtils.isEmpty(expectedEndTime)) {
            sb.append(expectedEndTime).append("结束").append(" ");
        }

//        if (!StringUtils.isEmpty(expectedStartTime)) {
//            sb.append(expectedStartTime).append("再开始").append(" ");
//        }

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        List<AbstractInstructionBean> instructionBeanList = new ArrayList<>();
        if (CollectionUtils.isEmpty(accountList) || accountList.size() <= 1) {
            String descriptionSuffix = "";
            SubStopTaskInstructionBean instructionBean = new SubStopTaskInstructionBean(this.getInstructionId(), this.getChatGroup(), this.getCreator(), CollectionUtils.isEmpty(accountList)? null: accountList.get(0), this.productModePairList, this.taskType, this.taskNameEqualList, this.taskNameContainList, this.taskNameNotContainList, this.taskNameSuffixList, this.taskCreateTimeBoundStart, this.taskCreateTimeBoundEnd, this.filteredTenantLinePairList, this.expectedStartTime, this.expectedEndTime, descriptionSuffix);
            instructionBeanList.add(instructionBean);
        } else {
            for (int i = 0; i < accountList.size(); i++) {
                String descriptionSuffix = "";
                if (instructionBeanList.size() > 1) {
                    descriptionSuffix += "\n----------\n" + this.toDescription();
                }
                String account = accountList.get(i);
                SubStopTaskInstructionBean instructionBean = new SubStopTaskInstructionBean(this.getInstructionId() + "_" + i, this.getChatGroup(), this.getCreator(), account, this.productModePairList, this.taskType, this.taskNameEqualList, this.taskNameContainList, this.taskNameNotContainList, this.taskNameSuffixList, this.taskCreateTimeBoundStart, this.taskCreateTimeBoundEnd, this.filteredTenantLinePairList, this.expectedStartTime, this.expectedEndTime, descriptionSuffix);
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
        if (CollectionUtils.isEmpty(accountList)) {
            return new CheckResult(false,"账号为空");
        }
        if (taskType == null) {
            return new CheckResult(false,"任务类型为空");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        return null;
    }
}
