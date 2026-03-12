package org.example.instruction.bean;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.ScriptStatus;
import org.example.chat.bean.baize.TaskTemplate;
import org.example.chat.bean.baize.script.Script;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;
import org.example.utils.ThreadUtils;

import java.util.*;
import java.util.concurrent.Callable;

public class SubStopScriptInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_STOP_SCRIPT;
    private static final String DUMMY_SCRIPT_NAME = "停用替换话术-不可营销";
    private static final long MILLIS = 100;

    private String scriptName;

    public SubStopScriptInstructionBean(String instructionId, ChatGroup chatGroup, String creator, String scriptName) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.scriptName = scriptName;
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

        sb.append("话术名称：");
        if (!StringUtils.isEmpty(scriptName)) {
            sb.append(scriptName);
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
        if (StringUtils.isEmpty(scriptName)) {
            return new CheckResult(false, "话术名称为空");
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        try {
            CheckResult checkResult = this.checkValid();
            if (checkResult.isCorrect()) {
                Callable<ExecutionResult> callable = () -> {
                    BaizeClient client = BaizeClientFactory.getBaizeClient();

                    List<Script> scriptList = client.adminGetScriptList(ScriptStatus.ACTIVE);

                    Script script = InstructionUtils.filterScript(scriptList, scriptName);
                    if (script == null) {
                        String msg = "执行指令失败！目标话术【" + scriptName + "】不存在或未生效" + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }

                    List<TaskTemplate> taskTemplateList = client.adminGetTaskTemplateListByScriptStringId(script.getScriptStringId());
                    taskTemplateList = InstructionUtils.filterTaskTemplate(taskTemplateList, scriptName);
                    if (!CollectionUtils.isEmpty(taskTemplateList)) {
                        // 绑定了账号的任务模板，需要改绑替换话术后停用任务模板，再解绑话术
                        Script dummyScript = InstructionUtils.filterScript(scriptList, DUMMY_SCRIPT_NAME);
                        if (dummyScript == null) {
                            String msg = "执行指令失败！替换话术【" + DUMMY_SCRIPT_NAME + "】不存在或未生效" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        // 改绑替换话术后停用任务模板
                        Set<String> groupIdSet = new HashSet<>();
                        for (TaskTemplate taskTemplate: taskTemplateList) {
                            taskTemplate.setScriptStringId(dummyScript.getScriptStringId());
                            taskTemplate.setSpeechCraftId(dummyScript.getId());
                            taskTemplate.setSpeechCraftName(dummyScript.getScriptName());
                            taskTemplate.setVersion(dummyScript.getVersion());
                            taskTemplate.setScriptSmsInfoList(Collections.emptyList());
                            taskTemplate.setTemplateStatus("1"); // 停用模板
                            client.adminSaveTaskTemplate(taskTemplate); // 更新模板

                            client.addTraceInfo(this.getInstructionType().getName(), ImmutableMap.of("action", "停用模板", "templateId", taskTemplate.getId()));

                            groupIdSet.add(taskTemplate.getGroupId());
                        }
                        // 解绑话术
                        for (String groupId: groupIdSet) {
                            client.adminRemoveRelatedScript(groupId, script.getId(), script.getScriptStringId());

                            client.addTraceInfo(this.getInstructionType().getName(), ImmutableMap.of("action", "解绑话术", "groupId", groupId, "scriptId", script.getId()));
                        }

                        ThreadUtils.sleep(MILLIS);
                    }

                    boolean success = client.stopScript(script.getId());
                    String msg;
                    if (success) {
                        client.addTraceInfo(this.getInstructionType().getName(), ImmutableMap.of("action", "停用话术", "scriptId", script.getId()));

                        msg = "执行指令成功！\n" + this.toDescription();
                    } else {
                        msg = "执行指令失败！\n" + this.toDescription();
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
            String msg = "执行指令失败！\n" + this.toDescription();
            Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
            return new CallableInfo(callable, 0);
        }
    }
}
