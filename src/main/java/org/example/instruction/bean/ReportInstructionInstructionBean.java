package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.ExecuteInstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReportInstructionInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_REPORT_INSTRUCTION;
    private String instructionIdToExecute;

    public ReportInstructionInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String instructionIdToExecute) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.instructionIdToExecute = instructionIdToExecute;
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

        sb.append("指令ID：");
        if (!StringUtils.isEmpty(instructionIdToExecute)) {
            sb.append(instructionIdToExecute);
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
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    ChatGroup chatGroup = this.getChatGroup();
                    if (StringUtils.isEmpty(instructionIdToExecute)) {
                        ExecuteInstructionUtils.reportAll(chatGroup, chatGroup.getRobotToken());
                        return new ExecutionResult(true, "");
                    } else {
                        AbstractInstructionBean instructionBean = ExecuteInstructionUtils.getUnexecutedInstructionBean(instructionIdToExecute);
                        if (instructionBean == null) {
                            instructionBean = ExecuteInstructionUtils.getOngoingInstructionBean(instructionIdToExecute);
                            if (instructionBean == null) {
                                String msg = "对应指令不存在或已被执行" + "\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                        }

                        String msg = instructionBean.toDescription();
                        return new ExecutionResult(false, msg); // false 是为了能够艾特人
                    }
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
