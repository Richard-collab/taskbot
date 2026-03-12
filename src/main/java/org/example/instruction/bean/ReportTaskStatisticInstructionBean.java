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

public class ReportTaskStatisticInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPORT_TASK_STATISTIC;

    private String account;
    private String strDate; // 外呼日期
    private List<String> intentionClassList; // 意向分类列表
    private Boolean outputAvgCallDuration; // 是否输出平均通时
    private Boolean outputTotalCallDuration; // 是否输出总通时


    public ReportTaskStatisticInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String account, String strDate,
            List<String> intentionClassList, Boolean outputAvgCallDuration, Boolean outputTotalCallDuration) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (StringUtils.isEmpty(strDate)) {
            strDate = DatetimeUtils.getStrDate(new Date());
        }
        if (CollectionUtils.isEmpty(intentionClassList)) {
            intentionClassList = Lists.newArrayList("A");
        }
        if (outputAvgCallDuration == null) {
            outputAvgCallDuration = false;
        }
        if (outputTotalCallDuration == null) {
            outputTotalCallDuration = false;
        }
        this.account = account;
        this.strDate = strDate;
        this.intentionClassList = intentionClassList;
        this.outputAvgCallDuration = outputAvgCallDuration;
        this.outputTotalCallDuration = outputTotalCallDuration;
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

        sb.append("意向分类：");
        if (!CollectionUtils.isEmpty(intentionClassList)) {
            sb.append(String.join("、", intentionClassList));
        }
        sb.append("\n");

        sb.append("输出平均通时：");
        if (outputAvgCallDuration != null) {
            sb.append(outputAvgCallDuration ? "是": "否");
        }
        sb.append("\n");

        sb.append("输出总通时：");
        if (outputTotalCallDuration != null) {
            sb.append(outputTotalCallDuration ? "是": "否");
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
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    ExecutionResult executionResult = GrcClient.reportTaskStatistic(account, strDate, intentionClassList, outputAvgCallDuration, outputTotalCallDuration);
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
