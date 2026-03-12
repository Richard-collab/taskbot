package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.utils.GrcClient;
import org.example.utils.CollectionUtils;
import org.example.utils.DatetimeUtils;
import org.example.utils.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class ReportTaskScriptStatisticInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPORT_TASK_SCRIPT_STATISTIC;

    private String account;
    private String strDate; // 外呼日期
    private List<String> taskNameList; // 任务名称列表


    public ReportTaskScriptStatisticInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String account, String strDate,
            List<String> taskNameList) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (StringUtils.isEmpty(strDate)) {
            strDate = DatetimeUtils.getStrDate(new Date());
        }
        this.account = account;
        this.strDate = strDate;
        this.taskNameList = taskNameList;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("群名称：").append(this.getChatGroup().getName()).append("\n");

        sb.append("创建人：");
        if (!StringUtils.isEmpty(this.getCreator())) {
            sb.append(this.getCreator());
        }
        sb.append("\n");

        sb.append("操作：").append(getInstructionType().getName().replace("操作类型_", "")).append("\n");

        sb.append("主账号：");
        if (account != null) {
            sb.append(account);
        }
        sb.append("\n");

        sb.append("外呼日期：");
        if (strDate != null) {
            sb.append(strDate);
        }
        sb.append("\n");

        sb.append("任务：");
        if (!CollectionUtils.isEmpty(taskNameList)) {
            sb.append(String.join("、", taskNameList));
        }
//        sb.append("\n");

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        if (StringUtils.isEmpty(account)) {
            return new CheckResult(false,"账号为空");
        }
        if (CollectionUtils.isEmpty(taskNameList)) {
            return new CheckResult(false,"任务名称为空");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    ExecutionResult executionResult = GrcClient.reportTaskScriptStatistic(account, strDate, taskNameList);
                    if (executionResult.isSuccess()) {
                        executionResult.setMsg("");
                    } else {
                        String msg = "执行指令失败！" + executionResult.getMsg() + "\n" + this.toDescription();
                        executionResult.setMsg(msg);
                    }
                    return executionResult;
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
