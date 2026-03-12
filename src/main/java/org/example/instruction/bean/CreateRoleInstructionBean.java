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

public class CreateRoleInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_CREATE_ROLE;
    private static final Set<Role> ROLES_TO_CREATE = Role.getCreatableRoleSet();

    private String mainAccount; // 主账号名称
    private String roleName;
    private Role sourceRole;
    private Set<String> ipSet;

    public CreateRoleInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String mainAccount, String roleName, Role sourceRole, Set<String> ipSet) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (mainAccount != null) {
            mainAccount = mainAccount.trim();
        }
        this.mainAccount = mainAccount;
        this.roleName = roleName;
        this.sourceRole = sourceRole;
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

        sb.append("操作：").append(this.getInstructionType().getName().replace("操作类型_", "")).append("\n");

        sb.append("主账号名称：");
        if (!StringUtils.isEmpty(mainAccount)) {
            sb.append(mainAccount);
        }
        sb.append("\n");

        sb.append("创建角色：");
        if (!StringUtils.isEmpty(roleName)) {
            sb.append(roleName);
        }
        sb.append("\n");

        sb.append("来源角色：");
        if (sourceRole != null) {
            sb.append(sourceRole.getCaption());
        }
        sb.append("\n");

        sb.append("IP：");
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
        if (StringUtils.isEmpty(this.getInstructionId())) {
            return new CheckResult(false,"指令ID为空");
        }
        if (StringUtils.isEmpty(mainAccount)) {
            return new CheckResult(false,"主账号名称为空");
        }
        if (StringUtils.isEmpty(roleName)) {
            return new CheckResult(false,"创建角色名称为空");
        }
        if (sourceRole == null) {
            return new CheckResult(false,"来源角色名称为空");
        }
        if (!ROLES_TO_CREATE.contains(sourceRole)) {
            return new CheckResult(false,"不支持从【" + sourceRole.getCaption() + "】新建角色");
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
        Callable<ExecutionResult> callable = () -> {
            ExecutionResult result;
            try {
                CheckResult checkResult = this.checkValid();
                if (checkResult.isCorrect()) {
                    BaizeClient adminClient = BaizeClientFactory.getBaizeClient();

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

                    // 创建商户端角色，并设置IP白名单
                    int roleId = mainClient.addRole(roleName, sourceRole);
                    if (roleId < 0) {
                        String msg = "指令执行失败！【" + roleName + "】角色创建失败";
                        return new ExecutionResult(false, msg);
                    }

                    if (!CollectionUtils.isEmpty(ipSet)) {
                        boolean setIpSuccess = mainClient.setIpList(roleId, ipSet);
                        if (!setIpSuccess) {
                            String msg = "指令执行失败！【" + roleName + "】角色创建成功，但设置IP失败";
                            return new ExecutionResult(false, msg);
                        }
                    }

                    StringBuilder sb = new StringBuilder()
                            .append("账号创建成功！").append("\n")
                            .append("主账号：").append(mainAccount).append("\n")
                            .append("创建角色：").append(roleName).append("\n")
                            .append("来源角色：").append(sourceRole.getCaption()).append("\n");
                    sb.append("IP：");
                    if (!CollectionUtils.isEmpty(ipSet)) {
                        sb.append(String.join("、", ipSet));
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
