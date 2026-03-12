package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class SetAccountOperatorParamInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_SET_ACCOUNT_OPERATOR_PARAM;
    private String account;
//    private String product;
//    private String entranceMode;
    private Set<OutboundType> dataStatisticOutboundTypeSet;
    private String taskCallbackUrl;
    private Set<OutboundType> callbackOutboundTypeSet;
    private Set<CallbackStatus> callbackStatusSet;
    private Set<OutboundCallbackField> outboundCallbackFieldSet;
    private String quicklyCallbackUrl;
    private String newCallbackUrl;
    private String txtUpdateCallbackUrl;
    private String mSmsCallbackUrl;
    private String oldCallBackUrl;
    private Set<SmsCallbackField> smsCallbackFieldSet;
    private String smsCallbackUrl;
    private String upSmsCallbackUrl;
    private Set<String> ipSet;

    public SetAccountOperatorParamInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String account,
//            String product, String entranceMode,
            Set<OutboundType> dataStatisticOutboundTypeSet, String taskCallbackUrl, Set<OutboundType> callbackOutboundTypeSet,
            Set<CallbackStatus> callbackStatusSet, Set<OutboundCallbackField> outboundCallbackFieldSet,
            String quicklyCallbackUrl, String newCallbackUrl, String txtUpdateCallbackUrl, String mSmsCallbackUrl,
            String oldCallBackUrl, Set<SmsCallbackField> smsCallbackFieldSet, String smsCallbackUrl, String upSmsCallbackUrl,
            Set<String> ipSet) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);

        if (outboundCallbackFieldSet == null) {
            outboundCallbackFieldSet = new HashSet<>();
        }
        outboundCallbackFieldSet.add(OutboundCallbackField.phone); // 手机号必填

        if (smsCallbackFieldSet == null) {
            smsCallbackFieldSet = new HashSet<>();
        }
        smsCallbackFieldSet.add(SmsCallbackField.smsPhone); // 手机号必填

        if (ipSet == null) {
            ipSet = new LinkedHashSet<>();
        }

        this.account = account;
//        this.product = product;
//        this.entranceMode = entranceMode;
        this.dataStatisticOutboundTypeSet = dataStatisticOutboundTypeSet;
        this.taskCallbackUrl = taskCallbackUrl;
        this.callbackOutboundTypeSet = callbackOutboundTypeSet;
        this.callbackStatusSet = callbackStatusSet;
        this.outboundCallbackFieldSet = outboundCallbackFieldSet;
        this.quicklyCallbackUrl = quicklyCallbackUrl;
        this.newCallbackUrl = newCallbackUrl;
        this.txtUpdateCallbackUrl = txtUpdateCallbackUrl;
        this.mSmsCallbackUrl = mSmsCallbackUrl;
        this.oldCallBackUrl = oldCallBackUrl;
        this.smsCallbackFieldSet = smsCallbackFieldSet;
        this.smsCallbackUrl = smsCallbackUrl;
        this.upSmsCallbackUrl = upSmsCallbackUrl;
        this.ipSet = ipSet;
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

        sb.append("账号：");
        if (!StringUtils.isEmpty(account)) {
            sb.append(account);
        }
        sb.append("\n");

//        sb.append("业务：");
//        if (!StringUtils.isEmpty(product)) {
//            sb.append(product);
//        }
//        if (!StringUtils.isEmpty(entranceMode)) {
//            sb.append(entranceMode);
//        }
//        sb.append("\n");

        sb.append("查询范围：");
        if (!CollectionUtils.isEmpty(dataStatisticOutboundTypeSet)) {
            sb.append(dataStatisticOutboundTypeSet.stream().map(x -> x.getCaption()).collect(Collectors.joining("、")));
        }
        sb.append("\n");

        sb.append("任务回调地址：");
        if (!StringUtils.isEmpty(taskCallbackUrl)) {
            sb.append(taskCallbackUrl);
        }
        sb.append("\n");

        sb.append("通话回调范围：");
        if (!CollectionUtils.isEmpty(callbackOutboundTypeSet)) {
            sb.append(callbackOutboundTypeSet.stream().map(x -> x.getCaption()).collect(Collectors.joining("、")));
        }
        sb.append("\n");

        sb.append("回调状态：");
        if (!CollectionUtils.isEmpty(callbackStatusSet)) {
            sb.append(callbackStatusSet.stream().map(x -> x.getCaption()).collect(Collectors.joining("、")));
        }
        sb.append("\n");

        sb.append("回调字段：");
        if (!CollectionUtils.isEmpty(outboundCallbackFieldSet)) {
            sb.append(outboundCallbackFieldSet.stream().map(x -> x.getCaption()).collect(Collectors.joining("、")));
        }
        sb.append("\n");

        sb.append("快速回调接口：");
        if (!StringUtils.isEmpty(quicklyCallbackUrl)) {
            sb.append(quicklyCallbackUrl);
        }
        sb.append("\n");

        sb.append("话后回调接口（新）：");
        if (!StringUtils.isEmpty(newCallbackUrl)) {
            sb.append(newCallbackUrl);
        }
        sb.append("\n");

        sb.append("文本补推接口：");
        if (!StringUtils.isEmpty(txtUpdateCallbackUrl)) {
            sb.append(txtUpdateCallbackUrl);
        }
        sb.append("\n");

        sb.append("M短信接口：");
        if (!StringUtils.isEmpty(mSmsCallbackUrl)) {
            sb.append(mSmsCallbackUrl);
        }
        sb.append("\n");

        sb.append("通话回调接口（旧）：");
        if (!StringUtils.isEmpty(oldCallBackUrl)) {
            sb.append(oldCallBackUrl);
        }
        sb.append("\n");

        sb.append("短信回调字段：");
        if (!CollectionUtils.isEmpty(smsCallbackFieldSet)) {
            sb.append(smsCallbackFieldSet.stream().map(x -> x.getCaption()).collect(Collectors.joining("、")));
        }
        sb.append("\n");

        sb.append("短信回调地址：");
        if (!StringUtils.isEmpty(smsCallbackUrl)) {
            sb.append(smsCallbackUrl);
        }
        sb.append("\n");

        sb.append("上行短信回调地址：");
        if (!StringUtils.isEmpty(upSmsCallbackUrl)) {
            sb.append(upSmsCallbackUrl);
        }
        sb.append("\n");

        sb.append("IP配置：");
        if (!CollectionUtils.isEmpty(ipSet)) {
            sb.append(String.join("、", ipSet));
        }
        sb.append("\n");

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
        if (CollectionUtils.isEmpty(dataStatisticOutboundTypeSet)) {
            return new CheckResult(false, "查询范围为空");
        }
        if (!StringUtils.isEmpty(taskCallbackUrl) && !StringUtils.isUrl(taskCallbackUrl)) {
            return new CheckResult(false,"非法的任务回调地址：" + taskCallbackUrl);
        }
        if (CollectionUtils.isEmpty(callbackOutboundTypeSet)) {
            return new CheckResult(false, "通话回调范围为空");
        }
        if (!StringUtils.isEmpty(quicklyCallbackUrl) && !StringUtils.isUrl(quicklyCallbackUrl)) {
            return new CheckResult(false,"非法的快速回调地址：" + quicklyCallbackUrl);
        }
        if (!StringUtils.isEmpty(newCallbackUrl) && !StringUtils.isUrl(newCallbackUrl)) {
            return new CheckResult(false,"非法的话后回调接口（新）：" + newCallbackUrl);
        }
        if (!StringUtils.isEmpty(txtUpdateCallbackUrl) && !StringUtils.isUrl(txtUpdateCallbackUrl)) {
            return new CheckResult(false,"非法的文本补推接口：" + txtUpdateCallbackUrl);
        }
        if (!StringUtils.isEmpty(mSmsCallbackUrl) && !StringUtils.isUrl(mSmsCallbackUrl)) {
            return new CheckResult(false,"非法的M短信接口：" + mSmsCallbackUrl);
        }
        if (!StringUtils.isEmpty(oldCallBackUrl) && !StringUtils.isUrl(oldCallBackUrl)) {
            return new CheckResult(false,"非法的通话回调接口（旧）：" + oldCallBackUrl);
        }
        if (!StringUtils.isEmpty(smsCallbackUrl) && !StringUtils.isUrl(smsCallbackUrl)) {
            return new CheckResult(false,"非法的短信回调地址：" + smsCallbackUrl);
        }
        if (!StringUtils.isEmpty(upSmsCallbackUrl) && !StringUtils.isUrl(upSmsCallbackUrl)) {
            return new CheckResult(false,"非法的上行短信回调地址：" + upSmsCallbackUrl);
        }
        if (CollectionUtils.isEmpty(callbackStatusSet)) {
            return new CheckResult(false, "回调状态为空");
        }
        if (!CollectionUtils.isEmpty(ipSet)) {
            for (String ip: ipSet) {
                if (!StringUtils.isIp(ip)) {
                    return new CheckResult(false,"非法的IP：" + ip);
                }
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
                    try {
                        BaizeClient adminClient = BaizeClientFactory.getBaizeClient();

                        // 登录主账号
                        BaizeClient mainClient;
                        try {
                            mainClient = BaizeClientFactory.getBaizeClient(account);
                        } catch (Exception e) {
                            String msg = "指令执行失败！主账号【" + account + "】登录失败" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                        int accountId = mainClient.getAccountId();
                        AccountOperatorParam accountOperatorParam = adminClient.adminGetAccountOperatorParam(accountId);
                        if (accountOperatorParam == null) {
                            String msg = "指令执行失败！查询现有回调信息出错" + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }
                        List<String> ipList = accountOperatorParam.getWhiteIps();
                        ipSet.addAll(ipList);
                        boolean success = adminClient.adminSetAccountOperatorParam(accountId, dataStatisticOutboundTypeSet, taskCallbackUrl,
                                callbackOutboundTypeSet, callbackStatusSet, outboundCallbackFieldSet, quicklyCallbackUrl, newCallbackUrl,
                                txtUpdateCallbackUrl, mSmsCallbackUrl, oldCallBackUrl, smsCallbackFieldSet, smsCallbackUrl,
                                upSmsCallbackUrl, ipSet);
                        String msg;
                        if (success) {
                            msg = "执行指令成功！\n" + this.toDescription();
                        } else {
                            msg = "执行指令失败！\n" + this.toDescription();
                        }
                        return new ExecutionResult(success, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
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
            String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
            Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
            return new CallableInfo(callable, 0);
        }
    }
}
