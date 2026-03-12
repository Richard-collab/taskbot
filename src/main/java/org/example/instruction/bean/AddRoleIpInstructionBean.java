package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.Role;
import org.example.chat.bean.baize.RoleInfo;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class AddRoleIpInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_ADD_ROLE_IP;

    private String mainAccount; // 主账号名称
    private String roleName;
    private Set<String> ipSet;

    public AddRoleIpInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String mainAccount, String roleName, Set<String> ipSet) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        if (mainAccount != null) {
            mainAccount = mainAccount.trim();
        }
        this.mainAccount = mainAccount;
        this.roleName = roleName;
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

        sb.append("角色：");
        if (!StringUtils.isEmpty(roleName)) {
            sb.append(roleName);
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
        if (CollectionUtils.isEmpty(ipSet)) {
            return new CheckResult(false,"IP为空");
        }
        for (String ip: ipSet) {
            if (!StringUtils.isIp(ip)) {
                return new CheckResult(false,"非法的IP：" + ip);
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
//                    BaizeClient adminClient = BaizeClientFactory.getBaizeClient();

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

                    // 绑定IP
                    Map<String, RoleInfo> roleName2roleInfo = roleInfoList.stream().collect(Collectors.toMap(roleInfo -> roleInfo.getRoleName(), roleInfo -> roleInfo, (oldValue, newValue) -> newValue));
                    RoleInfo roleInfo = roleName2roleInfo.get(roleName);
                    if (roleInfo == null) {
                        String msg = "指令执行失败！账号角色【" + roleName + "】不存在";
                        return new ExecutionResult(false, msg);
                    }
                    int roleId = roleInfo.getId();
                    List<String> existedIpList = mainClient.getIpList(roleId);
                    if (existedIpList == null) {
                        String msg = "指令执行失败！账号角色【" + roleName + "】获取现有IP列表失败";
                        return new ExecutionResult(false, msg);
                    }
                    Set<String> allIpSet = new HashSet<>(existedIpList);
                    allIpSet.addAll(ipSet);
                    boolean setIpSuccess = mainClient.setIpList(roleId, allIpSet);
                    if (!setIpSuccess) {
                        String msg = "指令执行失败！【" + roleName + "】角色创建成功，但设置IP失败";
                        return new ExecutionResult(false, msg);
                    }

                    String msg = new StringBuilder()
                            .append("添加角色IP成功！").append("\n")
                            .append("主账号：").append(mainAccount).append("\n")
                            .append("角色：").append(roleName).append("\n")
                            .append("IP：").append(String.join("、", ipSet))
                            .toString();
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
