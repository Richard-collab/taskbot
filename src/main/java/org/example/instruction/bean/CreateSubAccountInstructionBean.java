package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CreateSubAccountInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_CREATE_SUB_ACCOUNT;

    private String mainAccount; // 主账号名称
    private List<AccountInfo> accountInfoList;

    public CreateSubAccountInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String mainAccount, List<AccountInfo> accountInfoList) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (mainAccount != null) {
            mainAccount = mainAccount.trim();
        }
        this.mainAccount = mainAccount;
        this.accountInfoList = accountInfoList;
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

        sb.append("主账号名称：");
        if (!StringUtils.isEmpty(mainAccount)) {
            sb.append(mainAccount);
        }
        sb.append("\n");

        if (!CollectionUtils.isEmpty(accountInfoList)) {
            for (AccountInfo accountInfo: accountInfoList) {
                sb.append(accountInfo.getRoleName()).append("账号：")
                        .append(accountInfo.getAccount()).append("/").append(accountInfo.getPassword()).append("/").append(accountInfo.getName())
                        .append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        if (StringUtils.isEmpty(this.getInstructionId())) {
            return new CheckResult(false,"指令ID为空");
        }
        if (StringUtils.isEmpty(mainAccount)) {
            return new CheckResult(false,"主账号名称为空");
        }
        if (CollectionUtils.isEmpty(accountInfoList)
                || accountInfoList.stream().anyMatch(x -> !x.checkValid())) {
            return new CheckResult(false,"子账号信息不全");
        }
        List<String> accountList = accountInfoList.stream().map(x -> x.getAccount()).collect(Collectors.toCollection(ArrayList::new));
        accountList.add(mainAccount);
        if (accountList.size() != accountList.stream().distinct().count()) {
            return new CheckResult(false,"账号有重复");
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
                    // 登录主账号
                    BaizeClient mainClient;
                    try {
                        mainClient = BaizeClientFactory.getBaizeClient(mainAccount);
                    } catch (Exception e) {
                        String msg = "指令执行失败！主账号【" + mainAccount + "】登录失败";
                        return new ExecutionResult(false, msg);
                    }

                    // 获取角色列表
                    List<RoleInfo> roleInfoList = mainClient.getRoleInfoList();
                    if (roleInfoList == null) {
                        String msg = "指令执行失败！账号角色获取失败";
                        return new ExecutionResult(false, msg);
                    }

                    // 获取账号列表
                    List<UserInfo> userInfoList = mainClient.getUserInfoList();
                    Set<String> accountSet = userInfoList.stream()
                            .map(userInfo -> userInfo.getAccount()).collect(Collectors.toSet());

                    // 创建子账号
                    Map<String, RoleInfo> roleName2roleInfo = roleInfoList.stream().collect(Collectors.toMap(roleInfo -> roleInfo.getRoleName(), roleInfo -> roleInfo, (oldValue, newValue) -> newValue));
                    Set<String> roleNameSet = roleName2roleInfo.keySet();
                    for (AccountInfo accountInfo: accountInfoList) {
                        String roleName = accountInfo.getRoleName();
                        if (!roleNameSet.contains(roleName)) {
                            String msg = "指令执行失败！账号角色【" + roleName + "】不存在";
                            return new ExecutionResult(false, msg);
                        }
                        String account = accountInfo.getAccount();
                        if (accountSet.contains(account)) {
                            String msg = "指令执行失败！子账号【" + account + "】已存在";
                            return new ExecutionResult(false, msg);
                        }
                    }
                    for (AccountInfo accountInfo: accountInfoList) {
                        String roleName = accountInfo.getRoleName();
                        RoleInfo roleInfo = roleName2roleInfo.get(roleName);
                        int roleId = roleInfo.getId();
                        boolean addSubUserSuccess = mainClient.addSubUser(accountInfo.getAccount(), accountInfo.getPassword(), accountInfo.getPassword(), accountInfo.getName(), roleId);
                        if (!addSubUserSuccess) {
                            String msg = "指令执行失败！" + accountInfo.getRoleName() + "子账号【" + accountInfo.getAccount() + "】创建失败";
                            return new ExecutionResult(false, msg);
                        }
                    }

                    StringBuilder sb = new StringBuilder()
                            .append("账号创建成功！").append("\n")
                            .append("主账号：").append(mainAccount).append("\n");
                    for (AccountInfo accountInfo: accountInfoList) {
                        sb.append(accountInfo.getRoleName()).append("账号：")
                                .append(accountInfo.getAccount()).append("/").append(accountInfo.getPassword()).append("/").append(accountInfo.getName())
                                .append("\n");
                    }
                    String msg = sb.toString();
                    return new ExecutionResult(true, msg);
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
