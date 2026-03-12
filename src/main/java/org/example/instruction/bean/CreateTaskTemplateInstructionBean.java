package org.example.instruction.bean;

import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.*;
import org.example.chat.bean.baize.script.Script;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CreateTaskTemplateInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_CREATE_TASK_TEMPLATE;

    private String account; // 主账号
    private String templateName; // 模板名称
    private String comment; // 模板说明
    private String taskName; // 任务名称
    private TaskType taskType; // 任务类型
    private boolean nextDayCall; // 是否隔日续呼
    private String scriptName; // 话术名称
    private List<String> callTeamNameList; // 坐席组名称列表
    private PushType callTeamPushType = PushType.ROUND_ROBIN; // 推送方式
    private HandleType callTeamHandleType; // 处理方式
    private String lineRatio; // 集线比
    private OccupyRate occupyRate; // 占用等级
    private String virtualSeatRatio; // 虚拟坐席系数
    private List<Pair<String, String>> workStartEndTimePairList; // 拨打时段
    private boolean autoReCall = false; // 自动补呼

    public CreateTaskTemplateInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String account, String templateName,
            TaskType taskType, Boolean nextDayCall, String scriptName, List<String> callTeamNameList,
            HandleType callTeamHandleType, String lineRatio, OccupyRate occupyRate, String virtualSeatRatio,
            List<Pair<String, String>> workStartEndTimePairList) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (nextDayCall == null) {
            nextDayCall = false;
        }
        if (CollectionUtils.isEmpty(workStartEndTimePairList)) {
            workStartEndTimePairList = Lists.newArrayList(
                    new Pair<>("09:00", "12:00"),
                    new Pair<>("12:30", "19:00")
            );
        }
        if (taskType == TaskType.AI_MANUAL) {
            if (callTeamHandleType == null) {
                callTeamHandleType = HandleType.MONITOR;
            }
            if (StringUtils.isEmpty(lineRatio)) {
                lineRatio = "100";
            }
            if (occupyRate == null) {
                occupyRate = OccupyRate.MIDDLE;
            }
            if (StringUtils.isEmpty(virtualSeatRatio)) {
                virtualSeatRatio = "0.5";
            }
        }
        this.account = account;
        this.comment = templateName;
        this.taskName = templateName;
        this.templateName = templateName;
        this.taskType = taskType;
        this.nextDayCall = nextDayCall;
        this.scriptName = scriptName;
        this.callTeamNameList = callTeamNameList;
        this.callTeamHandleType = callTeamHandleType;
        this.lineRatio = lineRatio;
        this.occupyRate = occupyRate;
        this.virtualSeatRatio = virtualSeatRatio;
        this.workStartEndTimePairList = workStartEndTimePairList;
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

        sb.append("操作：").append(this.getInstructionType().getName().replace("操作类型_", "")).append("\n");

        sb.append("所属账号：");
        if (!StringUtils.isEmpty(account)) {
            sb.append(account);
        }
        sb.append("\n");

        sb.append("模板名称：");
        if (!StringUtils.isEmpty(templateName)) {
            sb.append(templateName);
        }
        sb.append("\n");

        sb.append("模板说明：");
        if (!StringUtils.isEmpty(comment)) {
            sb.append(comment);
        }
        sb.append("\n");

        sb.append("任务名称：");
        if (!StringUtils.isEmpty(taskName)) {
            sb.append(taskName);
        }
        sb.append("\n");

        sb.append("任务类型：");
        if (taskType != null) {
            sb.append(taskType.getCaption());
        }
        sb.append("\n");

        sb.append("是否隔日续呼：").append(nextDayCall? "是": "否").append("\n");

        sb.append("话术名称：");
        if (!StringUtils.isEmpty(scriptName)) {
            sb.append(scriptName);
        }
        sb.append("\n");

        if (taskType == TaskType.AI_MANUAL) {
            sb.append("坐席组：");
            if (!CollectionUtils.isEmpty(callTeamNameList)) {
                sb.append(String.join("、", callTeamNameList));
            }
            sb.append("\n");

            sb.append("推送方式：");
            if (callTeamPushType != null) {
                sb.append(callTeamPushType.getCaption());
            }
            sb.append("\n");

            sb.append("处理方式：");
            if (callTeamHandleType != null) {
                sb.append(callTeamHandleType.getCaption());
            }
            sb.append("\n");

            sb.append("集线比：");
            if (!StringUtils.isEmpty(lineRatio)) {
                sb.append(lineRatio);
            }
            sb.append("\n");

            sb.append("占用等级：");
            if (occupyRate != null) {
                sb.append(occupyRate.getCaption());
            }
            sb.append("\n");

            sb.append("虚拟坐席系数：");
            if (!StringUtils.isEmpty(virtualSeatRatio)) {
                sb.append(virtualSeatRatio);
            }
            sb.append("\n");
        }

        sb.append("拨打时段：");
        if (!CollectionUtils.isEmpty(workStartEndTimePairList)) {
            sb.append(workStartEndTimePairList.stream().map(pair -> pair.getKey() + "-" + pair.getValue()).collect(Collectors.joining("、")));
        }
        sb.append("\n");

        sb.append("自动补呼：").append(autoReCall? "是": "否").append("\n");

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
        if (StringUtils.isEmpty(templateName)) {
            return new CheckResult(false,"话术名称为空");
        }
        if (taskType == null) {
            return new CheckResult(false,"任务类型为空");
        }
        if (taskType == TaskType.AI_MANUAL) {
            if (CollectionUtils.isEmpty(callTeamNameList)) {
                return new CheckResult(false,"坐席组为空");
            }
            if (callTeamHandleType == null) {
                return new CheckResult(false,"处理方式为空");
            }
            if (StringUtils.isEmpty(lineRatio)) {
                return new CheckResult(false,"集线比为空");
            }
            try {
                float fLineRatio = Float.parseFloat(lineRatio);
                if (fLineRatio < 1 || fLineRatio > 20000) {
                    return new CheckResult(false, "非法的集线比");
                }
            } catch (Exception e) {
                return new CheckResult(false, "非法的集线比");
            }
            if (occupyRate == null) {
                return new CheckResult(false,"占用等级为空");
            }
            if (StringUtils.isEmpty(virtualSeatRatio)) {
                return new CheckResult(false,"虚拟坐席系数为空");
            }
            try {
                float fVirtualSeatRatio = Float.parseFloat(virtualSeatRatio);
                if (fVirtualSeatRatio < 0 || fVirtualSeatRatio > 100) {
                    return new CheckResult(false, "非法的虚拟坐席系数");
                }
            } catch (Exception e) {
                return new CheckResult(false, "非法的虚拟坐席系数");
            }
        }

        if (CollectionUtils.isEmpty(workStartEndTimePairList)) {
            return new CheckResult(false,"拨打时段为空");
        }

        for (Pair<String, String> pair: workStartEndTimePairList) {
            if (pair == null || StringUtils.isEmpty(pair.getKey()) || StringUtils.isEmpty(pair.getValue())
                    || !StringUtils.isArabicHourMinute(pair.getKey())
                    || !StringUtils.isArabicHourMinute(pair.getValue())) {
                return new CheckResult(false,"非法的拨打时段");
            }
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
                    BaizeClient client = BaizeClientFactory.getBaizeClient();

                    Map<String, String> mainAccount2GroupId = BaizeClientFactory.getBaizeClient().adminGetMainAccount2GroupId();
                    if (!mainAccount2GroupId.containsKey(account)) {
                        String msg = "指令执行失败！主账号【" + account + "】不存在\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                    String groupId = mainAccount2GroupId.get(account);

                    List<Tenant> tenantList = client.adminGetTenantList(account);
                    if (CollectionUtils.isEmpty(tenantList) || tenantList.size() != 1) {
                        String msg = "指令执行失败！主账号【" + account + "】查不到对应的商户信息\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                    int tenantId = tenantList.get(0).getId();

                    List<Script> scriptList = client.adminGetScriptList(ScriptStatus.ACTIVE);
                    if (CollectionUtils.isEmpty(scriptList)) {
                        String msg = "指令执行失败！获取生效中话术报错\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                    Map<String, Script> scriptName2script = scriptList.stream()
                            .collect(Collectors.toMap(x -> x.getScriptName(), y -> y, (o1, o2) -> o1));
                    if (!scriptName2script.containsKey(scriptName)) {
                        String msg = "指令执行失败！话术【" + scriptName + "】不存在或未生效\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                    Script script = scriptName2script.get(scriptName);

                    // 账号没有绑定话术的，先绑定话术
                    AccountScriptInfo accountScriptInfo = client.adminGetAccountScriptInfo(tenantId, groupId);
                    if (accountScriptInfo == null) {
                        String msg = "指令执行失败！主账号【" + account + "】查不到所绑定的话术信息\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }

                    Set<String> relatedScriptNameSet = accountScriptInfo.getRelatedScriptList().stream()
                            .map(x -> x.getScriptName()).collect(Collectors.toSet());
                    if (!relatedScriptNameSet.contains(scriptName)) {
                        boolean success = client.adminBindAccountScript(script.getId(), tenantId, groupId, script.getScriptStringId());
                        if (!success) {
                            String msg = "指令执行失败！主账号【" + account + "】绑定的话术【" + scriptName + "】失败\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }
                    }

                    if (!CollectionUtils.isEmpty(script.getSmsTriggerNames())) {
                        String msg = "指令执行失败！话术【" + scriptName + "】含有短信触发点，需要手工配置\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }

                    // 拼装模板信息
                    TaskTemplate.TaskTemplateBuilder taskTemplateBuilder = TaskTemplate.builder()
                            .groupId(groupId)
                            .templateName(templateName)
                            .comment(comment)
                            .taskName(taskName)
                            .taskType(taskType)
                            .nextDayCall(nextDayCall? 1: 0)
                            .scriptStringId(script.getScriptStringId())
                            .speechCraftId(script.getId())
                            .speechCraftName(script.getScriptName())
                            .version(script.getVersion());
                    if (taskType == TaskType.AI_MANUAL) {
                        List<CallTeam> callTeamList = client.adminGetCallTeamList(groupId);
                        Map<String, CallTeam> callTeamName2callTeam = callTeamList.stream()
                                .collect(Collectors.toMap(x -> x.getCallTeamName(), y -> y, (o1, o2) -> o1));
                        List<Integer> callTeamIdList = new ArrayList<>();
                        for (String callTeamName: callTeamNameList) {
                            if (!callTeamName2callTeam.containsKey(callTeamName)) {
                                String msg = "指令执行失败！坐席组【" + callTeamName + "】不存在\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                            CallTeam callTeam = callTeamName2callTeam.get(callTeamName);
                            callTeamIdList.add(callTeam.getId());
                        }
                        taskTemplateBuilder = taskTemplateBuilder
                                .callTeamHandleType(callTeamHandleType)
                                .callTeamIds(callTeamIdList)
                                .callTeamPushType(callTeamPushType)
                                .lineRatio(Float.parseFloat(lineRatio))
                                .virtualSeatRatio(Float.parseFloat(virtualSeatRatio))
                                .occupyRateId(occupyRate.getId());
                    }
                    TaskTemplate taskTemplate = taskTemplateBuilder
                            .startWorkTimeList(workStartEndTimePairList.stream().map(pair -> pair.getKey()).collect(Collectors.toList()))
                            .endWorkTimeList(workStartEndTimePairList.stream().map(pair -> pair.getValue()).collect(Collectors.toList()))
                            .autoReCall(autoReCall? 1: 0)
                            .build();

                    boolean success = client.adminSaveTaskTemplate(taskTemplate);
                    String msg;
                    if (success) {
                        msg = "执行指令成功！\n" + this.toDescription();
                    } else {
                        msg = "执行指令失败！\n" + this.toDescription();
                    }
                    return new ExecutionResult(success, msg);
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
}
