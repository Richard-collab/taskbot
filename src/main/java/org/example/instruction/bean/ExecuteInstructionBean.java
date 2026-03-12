package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.utils.ExecuteInstructionUtils;
import org.example.utils.StringUtils;

import java.util.List;
import java.util.concurrent.Callable;

public class ExecuteInstructionBean extends AbstractInstructionBean {

    private String instructionIdToExecute;

    public ExecuteInstructionBean(
            String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, String instructionIdToExecute) {
        super(instructionId, instructionType, chatGroup, creator);
        this.instructionIdToExecute = instructionIdToExecute;
    }

    public String getInstructionIdToExecute() {
        return instructionIdToExecute;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();
        switch (getInstructionType()) {
            case ACTION_REMOVE_INSTRUCTION: {
                sb.append("开始删除");
                break;
            }
            case ACTION_EXECUTE_INSTRUCTION: {
                sb.append("开始执行");
                break;
            }
            default:
                break;
        }
        sb.append("指令：").append(this.instructionIdToExecute);
        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        if (StringUtils.isEmpty(instructionIdToExecute)) {
            return new CheckResult(false,"指令ID为空");
        }
        if (ExecuteInstructionUtils.getUnexecutedInstructionBean(instructionIdToExecute) == null) {
            return new CheckResult(false,"对应指令不存在或已被执行");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        Callable<ExecutionResult> callable = () -> {
            ExecutionResult result;
            try {
                CheckResult checkResult = this.checkValid();
                if (checkResult.isCorrect()) {
                    switch (getInstructionType()) {
                        // TODO
                        default: {
                            String msg = "执行指令失败！不支持该类型任务\n" + this.toDescription();
                            result = new ExecutionResult(false, msg);
                            break;
                        }
                    }
                } else {
                    String msg = "执行指令失败！" + checkResult.getMsg() + "\n" + this.toDescription();
                    result = new ExecutionResult(false, msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                result = new ExecutionResult(false, msg);
            }
            return result;
        };
        return new CallableInfo(callable, 0);
    }
}
