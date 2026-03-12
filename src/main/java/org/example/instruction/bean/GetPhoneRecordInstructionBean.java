package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.utils.GrcClient;
import org.example.utils.CollectionUtils;
import org.example.utils.DatetimeUtils;
import org.example.utils.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

public class GetPhoneRecordInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_GET_PHONE_RECORD;

    private String strStartDate;
    private String strEndDate;
    private String contentContain;
    private Set<String> intentSet;
    private String lineCodeContain;


    public GetPhoneRecordInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String strStartDate, String strEndDate,
            String contentContain, Set<String> intentSet, String lineCodeContain) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (StringUtils.isEmpty(strStartDate)) {
            strStartDate = DatetimeUtils.getStrDate(new Date());
        }
        if (StringUtils.isEmpty(strEndDate)) {
            strEndDate = DatetimeUtils.getStrDate(new Date());
        }
        this.strStartDate = strStartDate;
        this.strEndDate = strEndDate;
        this.contentContain = contentContain;
        this.intentSet = intentSet;
        this.lineCodeContain = lineCodeContain;
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

        sb.append("外呼日期：");
        if (!StringUtils.isEmpty(strStartDate) || !StringUtils.isEmpty(strEndDate)) {
            if (Objects.equals(strStartDate, strEndDate)) {
                sb.append(strStartDate);
            } else {
                if (!StringUtils.isEmpty(strStartDate)) {
                    sb.append(strStartDate);
                }
                sb.append("~");
                if (!StringUtils.isEmpty(strEndDate)) {
                    sb.append(strEndDate);
                }
            }
        }
        sb.append("\n");

        sb.append("文本包含：");
        if (!StringUtils.isEmpty(contentContain)) {
            sb.append(contentContain);
        }
        sb.append("\n");

        sb.append("意图包含：");
        if (!CollectionUtils.isEmpty(intentSet)) {
            sb.append(String.join("、", intentSet));
        }
        sb.append("\n");

        sb.append("供应线路包含：");
        if (!StringUtils.isEmpty(lineCodeContain)) {
            sb.append(lineCodeContain);
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
        if (StringUtils.isEmpty(contentContain) && CollectionUtils.isEmpty(intentSet)) {
            return new CheckResult(false,"参数缺失");
        }
        if (!StringUtils.isEmpty(contentContain) && !CollectionUtils.isEmpty(intentSet)) {
            return new CheckResult(false,"不支持同时按文本和意图查询");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    ExecutionResult executionResult = GrcClient.reportPhoneRecord(strStartDate, strEndDate, contentContain, intentSet, lineCodeContain);
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
