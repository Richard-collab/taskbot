package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.AiTask;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.DatetimeUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CheckDataInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_CHECK_TASK_DATA;
//    static final Set<TaskStatus> TASK_STATUS_SET = ImmutableSet.of(TaskStatus.UNEXCUTED);

    private String account;
    private String product;
    private String entranceMode;

    public CheckDataInstructionBean(String instructionId, ChatGroup chatGroup, String creator, String account, String product, String entranceMode) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.account = account;
        this.product = product;
        this.entranceMode = entranceMode;
    }


    @Override
    public String toDescription() {
        Date date = new Date();
        String strDate = DatetimeUtils.getStrDate(date);

        StringBuilder sb = new StringBuilder();

        String tmpAccount = account;
        if (StringUtils.isEmpty(tmpAccount)) {
            tmpAccount = "";
        }
        sb.append("账号：").append(tmpAccount).append("\n");

        String tmpProduct = product;
        if (StringUtils.isEmpty(tmpProduct)) {
            tmpProduct = "";
        }
        String tmpEntranceMode = entranceMode;
        if (StringUtils.isEmpty(tmpEntranceMode)) {
            tmpEntranceMode = "";
        }
        if (!StringUtils.isEmpty(tmpProduct) || !StringUtils.isEmpty(tmpEntranceMode)) {
            sb.append("业务：").append(tmpProduct).append(tmpEntranceMode).append("\n");
        }

        sb.append("日期：").append(strDate);

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
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
        if (StringUtils.isEmpty(account)) {
            return new CheckResult(false,"账号为空");
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
                    BaizeClient client = BaizeClientFactory.getBaizeClient(account);
                    List<AiTask> taskList = client.findAiOutboundTasks();
//                    taskList = InstructionUtils.filterUnlockedAiTask(taskList);
//                    taskList = InstructionUtils.filterAiTask(taskList, TASK_STATUS_SET);

                    List<String> uncalledScriptNameBeforeList = new ArrayList<>();
                    Set<String> calledScriptNameSet = null;

                    StringBuilder sb = new StringBuilder();
                    sb.append("【请核对任务】").append("\n");
                    sb.append(this.toDescription()).append("\n");

                    if (CollectionUtils.isEmpty(taskList)) {
                        sb.append("没有符合要求的任务");
                    } else {
                        calledScriptNameSet = InstructionUtils.getYesterdayScriptNameSet(account);

                        Map<String, List<AiTask>> key2taskList = taskList.stream().collect(
                                Collectors.groupingBy(task -> getTaskKey(task), LinkedHashMap::new, Collectors.toList()));
                        int idx = 0;
                        for (Map.Entry<String, List<AiTask>> entry: key2taskList.entrySet()) {
                            String key = entry.getKey();
                            List<AiTask> tasks = entry.getValue();
                            idx++;
                            key = (key2taskList.keySet().size() == 1)? key: key.replace("话术", "话术" + idx);
                            sb.append(key).append("\n")
                                    .append("任务数量：").append(tasks.size()).append("\n")
                                    .append("名单量级：").append(tasks.stream().mapToInt(task -> task.getPhoneNum()).sum()).append("\n");

                            String scriptName = tasks.get(0).getSpeechCraftName();
                            if (calledScriptNameSet != null && !calledScriptNameSet.contains(scriptName)) {
                                uncalledScriptNameBeforeList.add(scriptName);
                            }
                        }
                    }

                    if (!CollectionUtils.isEmpty(taskList) && calledScriptNameSet == null) {
                        sb.append("\n").append("请注意：昨日话术查询出错，请加以核对今日话术").append("\n");
                    }
                    if (!CollectionUtils.isEmpty(uncalledScriptNameBeforeList)) {
                        sb.append("\n").append("请注意：话术【").append(String.join("】、【", uncalledScriptNameBeforeList)).append("】昨日未外呼，请加以核对");
                    }
                    String msg = sb.toString();
                    return new ExecutionResult(false, msg); // false 是为了能够艾特人
                } else  {
                    String msg = "执行指令失败！" + checkResult.getMsg() + "\n" + this.toDescription();
                    return new ExecutionResult(false, msg);
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

    private static String getTaskKey(AiTask task) {
        return new StringBuilder()
                .append("话术：").append(task.getSpeechCraftName()).append("\n")
                .append("模板id：").append(task.getTemplateId())
                .toString();
    }

}
