package org.example.instruction.bean;

import org.example.chat.bean.ChatGroup;
import org.example.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class OneByOneChangeTenantLineInstructionBean extends AbstractInstructionBean {

    private List<ChangeTenantLineInstructionBean> instructionBeanList;

    public OneByOneChangeTenantLineInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, List<ChangeTenantLineInstructionBean> instructionBeanList) {
        super(instructionId, null, chatGroup, creator);
        this.instructionBeanList = instructionBeanList;
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

        sb.append(instructionBeanList.stream().map(x -> x.toDetailDescription()).collect(Collectors.joining("\n\n")));

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return new ArrayList<>(instructionBeanList);
    }

    @Override
    public CheckResult checkValid() {
        for (ChangeTenantLineInstructionBean instructionBean: instructionBeanList) {
            CheckResult checkResult = instructionBean.checkValid();
            if (checkResult.isError()) {
                return checkResult;
            }
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    boolean success = true;
                    List<ExecutionResult> executionResultList = new ArrayList<>();
                    for (ChangeTenantLineInstructionBean instructionBean: instructionBeanList) {
                        ExecutionResult executionResult = instructionBean.getCallableInfo().getCallable().call();
                        executionResultList.add(executionResult);
                        if (!executionResult.isSuccess()) {
                            success = false;
                        }
                    }
                    String msg;
                    if (success) {
                        msg = "执行指令成功！\n" + this.toDescription();
                    } else {
                        msg = "执行指令失败！具体情况如下：\n" + executionResultList.stream().map(x -> x.getMsg()).collect(Collectors.joining("\n----------\n"));
                    }
                    return new ExecutionResult(success, msg);
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
