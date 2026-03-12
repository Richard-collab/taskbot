package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.HandleType;
import org.example.chat.bean.baize.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GetTaskTemplateInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_GET_TASK_TEMPLATE;

    private String templateId;

    public GetTaskTemplateInstructionBean(String instructionId, ChatGroup chatGroup, String creator, String templateId) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.templateId = templateId;
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

        sb.append("模板编号：");
        if (!StringUtils.isEmpty(templateId)) {
            sb.append(templateId);
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
        try {
            if (StringUtils.isEmpty(templateId) || Integer.parseInt(templateId) < 1) {
                return new CheckResult(false, "非法的任务模板ID");
            }
        } catch (NumberFormatException e) {
            return new CheckResult(false, "非法的任务模板ID");
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
                    TaskTemplate taskTemplate = client.adminGetTaskTemplateById(Integer.parseInt(templateId));
                    if (taskTemplate == null) {
                        String msg = "执行指令失败！找不到对应的任务模板" + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }

                    String account = taskTemplate.getAccount();
                    Map<String, String> mainAccount2GroupId = client.adminGetMainAccount2GroupId();
                    String groupId = mainAccount2GroupId.get(account);
                    List<ScriptSmsTemplate> smsTemplateList = client.adminGetSmsTemplateList(groupId, null);
                    Map<Integer, String> templateId2smsTemplateName = smsTemplateList.stream()
                            .collect(Collectors.toMap(x -> x.getId(), y -> y.getTemplateName(), (o1, o2) -> o1));

                    StringBuilder sb = new StringBuilder()
                            .append("操作：").append(getInstructionType().getName().replace("操作类型_", "")).append("\n")
                            .append("模板编号：").append(templateId).append("\n")
                            .append("所属账号：").append(account).append("\n")
                            .append("模板名称：").append(taskTemplate.getTemplateName()).append("\n")
                            .append("备注说明：").append(taskTemplate.getComment()).append("\n")
                            .append("任务名称：").append(taskTemplate.getTaskName()).append("\n")
                            .append("任务类型：").append(taskTemplate.getTaskType().getCaption()).append("\n")
                            .append("隔日续呼：").append(taskTemplate.getNextDayCall() == 1? "开": "关").append("\n")
                            .append("执行话术：").append(taskTemplate.getSpeechCraftName()).append("\n");

                    List<ScriptSmsInfo> scriptSmsInfoList = taskTemplate.getScriptSmsInfoList();
                    if (!CollectionUtils.isEmpty(scriptSmsInfoList)) {
                        sb.append("触发短信：")
                                .append(scriptSmsInfoList.stream().map(info -> "【" + info.getTriggerName() + "】" + templateId2smsTemplateName.get(info.getSmsTemplateId())).collect(Collectors.joining("、")))
                                .append("\n");
                    }

                    List<Integer> callTeamIdList = taskTemplate.getCallTeamIds();
                    if (!CollectionUtils.isEmpty(callTeamIdList)) {
                        List<CallTeam> callTeamList = client.adminGetCallTeamList(groupId);
                        sb.append("坐席组：")
                                .append(callTeamList.stream().filter(callTeam -> callTeamIdList.contains(callTeam.getId())).map(callTeam -> callTeam.getCallTeamName()).collect(Collectors.joining("、")))
                                .append("\n");
                    }

                    PushType pushType = taskTemplate.getCallTeamPushType();
                    if (pushType != null) {
                        sb.append("推送方式：").append(pushType.getCaption()).append("\n");
                    }

                    HandleType handleType = taskTemplate.getCallTeamHandleType();
                    if (handleType != null) {
                        sb.append("处理方式：").append(handleType.getCaption()).append("\n");
                    }

                    Float lineRatio = taskTemplate.getLineRatio();
                    if (lineRatio != null) {
                        sb.append("集线比：").append(lineRatio).append("\n");
                    }

                    OccupyRate occupyRate = taskTemplate.getOccupyRate();
                    if (occupyRate != null) {
                        sb.append("占用等级：").append(occupyRate.getCaption()).append("\n");
                    }

                    Float virtualSeatRatio = taskTemplate.getVirtualSeatRatio();
                    if (virtualSeatRatio != null) {
                        sb.append("虚拟坐席系数：").append(virtualSeatRatio).append("\n");
                    }

                    sb.append("拨打时段：");
                    if (!CollectionUtils.isEmpty(taskTemplate.getStartWorkTimeList())) {
                        sb.append(IntStream.range(0, taskTemplate.getStartWorkTimeList().size()).boxed().map(idx -> taskTemplate.getStartWorkTimeList().get(idx) + "-" + taskTemplate.getEndWorkTimeList().get(idx)).collect(Collectors.joining("、")));
                    }
                    sb.append("\n");

                    boolean autoReCall = taskTemplate.getAutoReCall() == 1;
                    sb.append("自动补呼：").append(autoReCall? "开": "关").append("\n");
                    if (autoReCall) {
                        sb.append("分配方式：").append(taskTemplate.getStrCallRatioType()).append("\n")
                                .append("补呼间隔：").append("第一次").append(taskTemplate.getFirstRecallTime()).append("分钟").append(" ")
                                .append("第二次").append(taskTemplate.getSecondRecallTime()).append("分钟").append("\n");
                    }

                    sb.append("挂机短信：").append("\n");
                    List<HangupSmsInfo> hangupSmsInfoList = taskTemplate.getHangupSmsInfoList();
                    if (!CollectionUtils.isEmpty(hangupSmsInfoList)) {
                        for (HangupSmsInfo hangupSmsInfo: hangupSmsInfoList) {
                            sb.append(hangupSmsInfo.getIntentionType()).append(" ").append(String.join("、", hangupSmsInfo.getLabelIds())).append(" ").append(templateId2smsTemplateName.get(hangupSmsInfo.getSmsTemplateId())).append("\n");
                        }
                    }

                    sb.append("发送排除：");
                    if (!CollectionUtils.isEmpty(taskTemplate.getHangupExcludedLabelList())) {
                        sb.append(String.join("、", taskTemplate.getHangupExcludedLabelList()));
                    }
                    sb.append("\n");

                    List<Integer> tenantBlackList = taskTemplate.getTenantBlackList();
                    sb.append("黑名单：");
                    if (!CollectionUtils.isEmpty(tenantBlackList)) {
                        List<BlacklistInfo> blacklistInfoList = client.adminGetTenantBlacklistInfoList(groupId);
                        sb.append(blacklistInfoList.stream().map(info -> info.getGroupName()).collect(Collectors.joining("、")));
                    }
                    sb.append("\n");

                    sb.append("屏蔽地区：");
                    if (!StringUtils.isEmpty(taskTemplate.getAllRestrictProvince()) || !StringUtils.isEmpty(taskTemplate.getAllRestrictCity())) {
                        sb.append("全部：");
                        if (!StringUtils.isEmpty(taskTemplate.getAllRestrictProvince())) {
                            sb.append(taskTemplate.getAllRestrictProvince().split(",", 0).length).append("省");
                        }
                        if (!StringUtils.isEmpty(taskTemplate.getAllRestrictCity())) {
                            sb.append(taskTemplate.getAllRestrictCity().split(",", 0).length).append("市");
                        }
                        sb.append(" ");
                    }
                    if (!StringUtils.isEmpty(taskTemplate.getYdRestrictProvince()) || !StringUtils.isEmpty(taskTemplate.getYdRestrictCity())) {
                        sb.append("移动：");
                        if (!StringUtils.isEmpty(taskTemplate.getYdRestrictProvince())) {
                            sb.append(taskTemplate.getYdRestrictProvince().split(",", 0).length).append("省");
                        }
                        if (!StringUtils.isEmpty(taskTemplate.getYdRestrictCity())) {
                            sb.append(taskTemplate.getYdRestrictCity().split(",", 0).length).append("市");
                        }
                        sb.append(" ");
                    }
                    if (!StringUtils.isEmpty(taskTemplate.getLtRestrictProvince()) || !StringUtils.isEmpty(taskTemplate.getLtRestrictCity())) {
                        sb.append("联通：");
                        if (!StringUtils.isEmpty(taskTemplate.getLtRestrictProvince())) {
                            sb.append(taskTemplate.getLtRestrictProvince().split(",", 0).length).append("省");
                        }
                        if (!StringUtils.isEmpty(taskTemplate.getLtRestrictCity())) {
                            sb.append(taskTemplate.getLtRestrictCity().split(",", 0).length).append("市");
                        }
                        sb.append(" ");
                    }
                    if (!StringUtils.isEmpty(taskTemplate.getDxRestrictProvince()) || !StringUtils.isEmpty(taskTemplate.getDxRestrictCity())) {
                        sb.append("电信：");
                        if (!StringUtils.isEmpty(taskTemplate.getDxRestrictProvince())) {
                            sb.append(taskTemplate.getDxRestrictProvince().split(",", 0).length).append("省");
                        }
                        if (!StringUtils.isEmpty(taskTemplate.getDxRestrictCity())) {
                            sb.append(taskTemplate.getDxRestrictCity().split(",", 0).length).append("市");
                        }
                        sb.append(" ");
                    }
                    if (!StringUtils.isEmpty(taskTemplate.getUnknownRestrictProvince()) || !StringUtils.isEmpty(taskTemplate.getUnknownRestrictCity())) {
                        sb.append("未知：");
                        if (!StringUtils.isEmpty(taskTemplate.getUnknownRestrictProvince())) {
                            sb.append(taskTemplate.getUnknownRestrictProvince().split(",", 0).length).append("省");
                        }
                        if (!StringUtils.isEmpty(taskTemplate.getUnknownRestrictCity())) {
                            sb.append(taskTemplate.getUnknownRestrictCity().split(",", 0).length).append("市");
                        }
                        sb.append(" ");
                    }
                    sb.append("\n");

                    String msg = sb.toString();
                    return new ExecutionResult(false, msg); // false 是为了能够艾特人
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
