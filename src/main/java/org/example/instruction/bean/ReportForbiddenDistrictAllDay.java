package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.utils.ExecuteInstructionUtils;
import org.example.chat.utils.ForbiddenDistrictUtils;
import org.example.utils.StringUtils;

import java.util.List;
import java.util.concurrent.Callable;

public class ReportForbiddenDistrictAllDay extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPORT_FORBID_DISTRICT_ALL_DAY;


    public ReportForbiddenDistrictAllDay(
            String instructionId, ChatGroup chatGroup, String creator) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
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
//        sb.append("\n");

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    ChatGroup chatGroup = this.getChatGroup();
                    ForbiddenDistrictUtils.report(chatGroup.getRobotToken());
                    return new ExecutionResult(true, "");
                };
                return new CallableInfo(callable, 0);
            } else {
                String msg = "执行指令失败！" + checkResult.getMsg() + "\n" + this.toDescription();
                Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
                return new CallableInfo(callable, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "执行指令失败！\n" + e + "\n" + this.toDescription();
            Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
            return new CallableInfo(callable, 0);
        }
    }
}
