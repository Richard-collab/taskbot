package org.example.instruction.bean;

import org.example.chat.bean.ChatGroup;
import org.example.chat.utils.ExecuteInstructionUtils;
import org.example.utils.ConstUtils;
import org.example.utils.DatetimeUtils;
import org.example.utils.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public abstract class AbstractInstructionBean implements Serializable {
    static final int WAIT_MS_FOR_CHECK = ConstUtils.WAIT_MS_FOR_CHECK;;

    private String instructionId;
    private InstructionType instructionType;
    private String creator;
    private ChatGroup chatGroup;
    private String strCreateDatetime;

    public AbstractInstructionBean(String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator) {
        if (instructionId == null) {
            instructionId = UUID.randomUUID().toString().replace("-", ""); // 生成随机UUID
        }
        if (creator == null) {
            creator = "";
        }
        this.instructionId = instructionId;
        this.instructionType = instructionType;
        this.chatGroup = chatGroup;
        this.creator = creator;
        this.strCreateDatetime = DatetimeUtils.getStrDatetime(new Date());
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    public ChatGroup getChatGroup() {
        return chatGroup;
    }

    public String getCreator() {
        return creator;
    }

    public String getStrCreateDatetime() {
        return strCreateDatetime;
    }

    public abstract String toDescription();

    public abstract List<AbstractInstructionBean> getSubInstructionBeanList();

    public abstract CheckResult checkValid();

    public abstract CallableInfo getCallableInfo();

//    public abstract List<CallableInfo> getCallableInfoList();

//    public List<ExecutionResult> execute() {
//        List<ExecutionResult> resultList = new ArrayList<>();
//        for (Pair<Callable<ExecutionResult>, Long> pair: getCallableDelaySecList()) {
//            ExecutionResult result;
//            try {
//                result = pair.getKey().call();
//            } catch (Exception e) {
//                String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
//                result = new ExecutionResult(false, msg);
//            }
//            resultList.add(result);
//        }
//        return resultList;
//    }


    public List<Future<ExecutionResult>> asyncExecute(String robotToken) {
        return ExecuteInstructionUtils.executeInstructionBean(robotToken, getInstructionId());
    }

    String getMsg(boolean success, int taskCount) {
        return getMsg(success, taskCount, "");
    }

    String getMsg(boolean success, int taskCount, String note) {
        StringBuilder sb = new StringBuilder();
        sb.append("指令执行");
        if (success) {
            sb.append("成功！");
        } else {
            sb.append("失败！");
        }
        sb.append("涉及任务数：").append(taskCount).append("\n");
        if (!StringUtils.isEmpty(note)) {
            sb.append(note).append("\n");
        }
        sb.append(this.toDescription());
        return sb.toString();
    }
}
