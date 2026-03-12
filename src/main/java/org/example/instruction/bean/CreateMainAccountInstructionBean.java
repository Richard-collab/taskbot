package org.example.instruction.bean;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.*;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CreateMainAccountInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_CREATE_MAIN_ACCOUNT;

    private static final Set<String> AVAILABLE_CREATOR_SET = Sets.newHashSet(
            "WangQi",
            "ChenZhengWei",
            "LuoQiJia",
            "ShiLin"
    );

    private static final Set<Role> ROLES_TO_CREATE = Role.getCreatableRoleSet();

    // 白泽运营ip列表
    private static final Set<String> BAIZE_IP_SET = Sets.newHashSet("192.168.106.208", "192.168.106.201");

    // 甲方ip列表
    private static final Set<String> JIAFANG_IP_SET = Sets.newHashSet("58.210.160.10", "111.230.87.86", "119.29.4.89", "183.6.117.35", "123.116.154.168", "58.210.160.10", "117.83.98.102", "192.168.106.208", "192.168.106.201", "192.168.229.24", "58.211.18.130", "61.155.147.50", "58.211.18.106", "58.211.18.114", "61.155.147.51", "58.210.160.10", "58.211.18.131", "192.168.208.235", "192.168.229.21", "61.155.147.44");

    private String tenantName; // 商户名称
    private String mainAccount; // 主账号名称
    private String mainPassword; // 主账号密码
    private String mainContacts; // 联系人
    private PhoneNumType phoneNumType; // 号码类型
    private String productName; // 产品名称
    private String secondIndustryName; // 二级行业名称，如：互联网小贷、保险
    private List<AccountInfo> accountInfoList;

    private Map<Role, Set<String>> role2ipSet;

    public CreateMainAccountInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String tenantName,
            String mainAccount, String mainPassword, String mainContacts, PhoneNumType phoneNumType, String productName,
            String secondIndustryName, List<AccountInfo> accountInfoList, Set<String> baizeIpSet, Set<String> jiafangIpSet) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);

        if (mainPassword == null) {
            mainPassword = "wangqi123@";
        }
        if (mainContacts == null) {
            mainContacts = "";
        }
        if (phoneNumType == null) {
            phoneNumType = PhoneNumType.PLAINTEXT_ALL_THE_TIME;
        }
        if (CollectionUtils.isEmpty(baizeIpSet)) {
            baizeIpSet = BAIZE_IP_SET;
        }
        if (CollectionUtils.isEmpty(jiafangIpSet)) {
            jiafangIpSet = JIAFANG_IP_SET;
        }
        if (accountInfoList == null) {
            accountInfoList = new ArrayList<>();
        }
        if (accountInfoList.stream().noneMatch(accountInfo -> accountInfo.getRole() == Role.BAIZE_YUNYING)
            && !StringUtils.isEmpty(mainAccount)) {
            accountInfoList.add(new AccountInfo(mainAccount + "sl", "shilin123", "石林", Role.BAIZE_YUNYING.getCaption()));
            accountInfoList.add(new AccountInfo(mainAccount + "wy", "wangyi123", "王一", Role.BAIZE_YUNYING.getCaption()));
            accountInfoList.add(new AccountInfo(mainAccount + "zjw", "zhangjiawei123", "张家唯", Role.BAIZE_YUNYING.getCaption()));
            accountInfoList.add(new AccountInfo(mainAccount + "zd", "zhaodi123", "赵娣", Role.BAIZE_YUNYING.getCaption()));
            accountInfoList.add(new AccountInfo(mainAccount + "ljh", "lijiahao123", "李佳豪", Role.BAIZE_YUNYING.getCaption()));
        }

        if (tenantName != null) {
            tenantName = tenantName.trim();
        }
        if (mainAccount != null) {
            mainAccount = mainAccount.trim();
        }
        if (mainPassword != null) {
            mainPassword = mainPassword.trim();
        }
        if (productName != null) {
            productName = productName.trim();
        }
        if (secondIndustryName != null) {
            secondIndustryName = secondIndustryName.trim();
        }
        this.tenantName = tenantName;
        this.mainAccount = mainAccount;
        this.mainPassword = mainPassword;
        this.mainContacts = mainContacts;
        this.phoneNumType = phoneNumType;
        this.productName = productName;
        this.secondIndustryName = secondIndustryName;
        this.accountInfoList = accountInfoList;
        this.role2ipSet = ImmutableMap.of(
                Role.BAIZE_YUNYING, baizeIpSet,
                Role.JIAFANG_GUANLIYUAN, jiafangIpSet
        );
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

        sb.append("商户名称：");
        if (!StringUtils.isEmpty(tenantName)) {
            sb.append(tenantName);
        }
        sb.append("\n");

        sb.append("主账号名称：");
        if (!StringUtils.isEmpty(mainAccount)) {
            sb.append(mainAccount);
        }
        sb.append("\n");

        sb.append("主账号密码：");
        if (!StringUtils.isEmpty(mainPassword)) {
            sb.append(mainPassword);
        }
        sb.append("\n");

        sb.append("联系人：");
        if (!StringUtils.isEmpty(mainContacts)) {
            sb.append(mainContacts);
        }
        sb.append("\n");

        sb.append("号码类型：");
        if (phoneNumType != null) {
            sb.append(phoneNumType.getCaption());
        }
        sb.append("\n");

        sb.append("产品名称：");
        if (!StringUtils.isEmpty(productName)) {
            sb.append(productName);
        }
        sb.append("\n");

        sb.append("二级行业：");
        if (!StringUtils.isEmpty(secondIndustryName)) {
            sb.append(secondIndustryName);
        }
        sb.append("\n");

        if (!CollectionUtils.isEmpty(accountInfoList)) {
            for (AccountInfo accountInfo: accountInfoList) {
                sb.append(accountInfo.getRoleName()).append("账号：")
                        .append(accountInfo.getAccount()).append("/").append(accountInfo.getPassword()).append("/").append(accountInfo.getName())
                        .append("\n");
            }
        }

        Set<String> baizeIpSet = role2ipSet.get(Role.BAIZE_YUNYING);
        sb.append("白泽IP：");
        if (!CollectionUtils.isEmpty(baizeIpSet)) {
            sb.append(String.join("、", baizeIpSet));
        }
        sb.append("\n");

        Set<String> jiafangIpSet = role2ipSet.get(Role.JIAFANG_GUANLIYUAN);
        sb.append("甲方IP：");
        if (!CollectionUtils.isEmpty(jiafangIpSet)) {
            sb.append(String.join("、", jiafangIpSet));
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
        if (!AVAILABLE_CREATOR_SET.contains(this.getCreator())) {
            return new CheckResult(false,"无权限的操作");
        }
        if (StringUtils.isEmpty(tenantName)) {
            return new CheckResult(false,"商户名称为空");
        }
        if (StringUtils.isEmpty(mainAccount)) {
            return new CheckResult(false,"主账号名称为空");
        }
        if (StringUtils.isEmpty(mainPassword)) {
            return new CheckResult(false,"主账号密码为空");
        }
        if (StringUtils.isEmpty(mainContacts)) {
            return new CheckResult(false,"联系人为空");
        }
        if (phoneNumType == null) {
            return new CheckResult(false,"号码类型为空");
        }
        if (!StringUtils.isEmpty(productName) && StringUtils.isEmpty(secondIndustryName)) {
            return new CheckResult(false,"二级行业为空");
        }
        if (CollectionUtils.isEmpty(accountInfoList)
                || accountInfoList.stream().anyMatch(x -> !x.checkValid())) {
            return new CheckResult(false,"子账号信息不全");
        }
        for (AccountInfo accountInfo: accountInfoList) {
            Role role = accountInfo.getRole();
            if (!ROLES_TO_CREATE.contains(role)) {
                return new CheckResult(false,"不支持新建" + role.getCaption() + "账号");
            }
        }
        if (accountInfoList.stream().noneMatch(accountInfo -> accountInfo.getRole() == Role.BAIZE_YUNYING)) {
            return new CheckResult(false,"缺少白泽运营账号信息");
        }
        if (accountInfoList.stream().noneMatch(accountInfo -> accountInfo.getRole() == Role.JIAFANG_GUANLIYUAN)) {
            return new CheckResult(false,"缺少甲方管理员账号信息");
        }
        List<String> accountList = accountInfoList.stream().map(x -> x.getAccount()).collect(Collectors.toCollection(ArrayList::new));
        accountList.add(mainAccount);
        if (accountList.size() != accountList.stream().distinct().count()) {
            return new CheckResult(false,"账号有重复");
        }
        Set<String> baizeIpSet = role2ipSet.get(Role.BAIZE_YUNYING);
        for (String ip: baizeIpSet) {
            if (!StringUtils.isIp(ip)) {
                return new CheckResult(false,"非法的白泽IP：" + ip);
            }
        }
        Set<String> jiafangIpSet = role2ipSet.get(Role.JIAFANG_GUANLIYUAN);
        for (String ip: jiafangIpSet) {
            if (!StringUtils.isIp(ip)) {
                return new CheckResult(false,"非法的甲方IP：" + ip);
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


                    // 获取商户信息
                    TenantInfoTool tenantInfoTool = adminClient.adminGetTenantInfoTool();
                    Integer tenantId = tenantInfoTool.getTenantIdByTenantName(tenantName);
                    String tenantNo = tenantInfoTool.getTenantNoByTenantId(tenantId);
                    if (tenantId == null || tenantNo == null) {
                        String msg = "指令执行失败！商户【" + tenantName + "】不存在";
                        return new ExecutionResult(false, msg);
                    }
                    // 获取行业信息
                    Integer secondIndustryId = null;
                    if (!StringUtils.isEmpty(productName)) {
                        IndustryInfoTool industryInfoTool = adminClient.adminGetIndustryInfoTool();
                        secondIndustryId = industryInfoTool.getSecondIndustryId(secondIndustryName);
                        if (secondIndustryId == null) {
                            String msg = "指令执行失败！二级行业【" + secondIndustryName + "】不存在";
                            return new ExecutionResult(false, msg);
                        }
                    }

                    // 获取主账号信息
                    Map<String, String> mainAccount2GroupId = adminClient.adminGetMainAccount2GroupId();
                    if (mainAccount2GroupId.containsKey(mainAccount)) {
                        String msg = "指令执行失败！主账号【" + mainAccount + "】已存在";
                        return new ExecutionResult(false, msg);
                    }

                    // 创建主账号
                    Boolean isForEncryptionPhones = null;
                    Boolean isForEncryptionAgain = null;
                    switch (phoneNumType) {
                        case CIPHERTEXT: {
                            isForEncryptionPhones = true;
                            break;
                        }
                        case PLAINTEXT_ALL_THE_TIME: {
                            isForEncryptionPhones = false;
                            isForEncryptionAgain = false;
                            break;
                        }
                        case PLAINTEXT_AFTER_DECRYPTION: {
                            isForEncryptionPhones = false;
                            isForEncryptionAgain = true;
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    boolean addMainUserSuccess = adminClient.adminAddMainUser(mainAccount, mainPassword, mainContacts, tenantId, isForEncryptionPhones, isForEncryptionAgain);
                    if (!addMainUserSuccess) {
                        String msg = "指令执行失败！主账号【" + mainAccount + "】创建失败";
                        return new ExecutionResult(false, msg);
                    }

                    // 登录主账号
                    BaizeClient mainClient = BaizeClientFactory.getBaizeClient(mainAccount, mainPassword);
                    int mainAccountId = mainClient.getAccountId();
                    String mainGroupId = mainClient.getGroupId();

                    // 创建商户端角色，并设置IP白名单
                    Map<Role, Integer> role2roleId = new HashMap<>();
                    for (Role role: ROLES_TO_CREATE) {
                        int roleId = mainClient.addRole(role);
                        if (roleId < 0) {
                            String msg = "指令执行失败！【" + role.getCaption() + "】角色创建失败";
                            return new ExecutionResult(false, msg);
                        }

                        role2roleId.put(role, roleId);

                        if (role2ipSet.containsKey(role)) {
                            Set<String> ipSet = role2ipSet.get(role);
                            boolean setIpSuccess = mainClient.setIpList(roleId, ipSet);
                            if (!setIpSuccess) {
                                String msg = "指令执行失败！【" + role.getCaption() + "】角色设置IP失败";
                                return new ExecutionResult(false, msg);
                            }
                        }
                    }

                    // 创建子账号
                    for (AccountInfo accountInfo: accountInfoList) {
                        int roleId = role2roleId.get(accountInfo.getRole());
                        boolean addSubUserSuccess = mainClient.addSubUser(accountInfo.getAccount(), accountInfo.getPassword(), accountInfo.getPassword(), accountInfo.getName(), roleId);
                        if (!addSubUserSuccess) {
                            String msg = "指令执行失败！" + accountInfo.getRoleName() + "子账号【" + accountInfo.getAccount() + "】创建失败";
                            return new ExecutionResult(false, msg);
                        }
                    }

                    AccountOperatorParam param = adminClient.adminGetAccountOperatorParam(mainAccountId);
                    StringBuilder sb = new StringBuilder()
                            .append("账号创建成功！").append("\n")
                            .append("请甲方务必注意：外呼前必须推数模拟真实业务情况进行全流程测试，测试没问题方可正式进行外呼").append("\n")
                            .append("主账号：").append(mainAccount).append("\n")
                            .append("联系人：").append(mainContacts).append("\n");
                    accountInfoList.stream().filter(x -> x.getRole() == Role.JIAFANG_GUANLIYUAN).forEach(accountInfo -> {
                        sb.append("甲方管理员账号：").append(accountInfo.getAccount()).append("\n")
                                .append("甲方管理员密码：").append(accountInfo.getPassword()).append("\n");
                    });
                    sb.append("商户id：").append(tenantId).append("\n")
                        .append("SALT：").append(param.getSalt()).append("\n")
                        .append("AES：").append(param.getAes()).append("\n")
                        .append("\n")
                        .append("请注意，该账号尚未配置回调地址，请后续完成回调配置后再开展业务！！！");
                    String msg = sb.toString();

                    // 创建并绑定产品
                    if (!StringUtils.isEmpty(productName)) {
                        List<Product> productList = adminClient.adminGetProductList(productName);
                        if (productList == null) {
                            msg = "产品【" + productName + "】查询失败，但" + msg;
                            return new ExecutionResult(false, msg);
                        }
                        productList = productList.stream()
                                .filter(x -> productName.equals(x.getProductName())).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(productList)) {
                            boolean saveProductSuccess = adminClient.adminSaveProduct(productName, secondIndustryId, secondIndustryName);
                            if (!saveProductSuccess) {
                                msg = "产品【" + productName + "】创建失败，但" + msg;
                                return new ExecutionResult(false, msg);
                            }

                            productList = adminClient.adminGetProductList(productName);
                            if (productList == null) {
                                msg = "产品【" + productName + "】查询失败，但" + msg;
                                return new ExecutionResult(false, msg);
                            }
                            productList = productList.stream()
                                    .filter(x -> productName.equals(x.getProductName())).collect(Collectors.toList());
                            if (CollectionUtils.isEmpty(productList)) {
                                msg = "产品【" + productName + "】创建失败，但" + msg;
                                return new ExecutionResult(false, msg);
                            }
                        }

                        int productId = productList.get(0).getId();

                        boolean saveProgramSuccess = adminClient.adminSaveProgram(productName, productId, productName, secondIndustryId, secondIndustryName, tenantId, mainGroupId);
                        if (!saveProgramSuccess) {
                            msg = "项目【" + productName + "】绑定失败，但产品【" + productName + "】创建成功，" + msg;
                            return new ExecutionResult(false, msg);
                        }
                    }

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
