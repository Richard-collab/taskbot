package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class EditTenantLineInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_EDIT_TENANT_LINE;

    private List<EditSingleTenantLineInstructionBean> singleInstructionBeanList;

    public EditTenantLineInstructionBean(String instructionId, ChatGroup chatGroup, String creator, List<EditSingleTenantLineInstructionBean> singleInstructionBeanList) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        singleInstructionBeanList.forEach(instructionBean -> instructionBean.setInstructionId(this.getInstructionId()));
        this.singleInstructionBeanList = singleInstructionBeanList;
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

        sb.append(singleInstructionBeanList.stream().map(x -> x.toDetailDescription()).collect(Collectors.joining("\n")));

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
//        return new ArrayList<>(singleInstructionBeanList);
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
        if (CollectionUtils.isEmpty(singleInstructionBeanList)) {
            return new CheckResult(false,"操作内容为空");
        }
        for (EditSingleTenantLineInstructionBean instructionBean: singleInstructionBeanList) {
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
                    for (EditSingleTenantLineInstructionBean instructionBean: singleInstructionBeanList) {
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
