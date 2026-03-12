package org.example.instruction.bean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.EnableStatus;
import org.example.chat.bean.baize.SupplyLine;
import org.example.chat.bean.baize.SupplyLineGroup;
import org.example.chat.bean.baize.TenantLine;
import org.example.chat.bean.baize.designedtenantline.DesignedDistrictSupplyLineInfo;
import org.example.chat.bean.baize.designedtenantline.DesignedSupplyLineGroup;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.DistrictUtils;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EditSingleTenantLineInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_EDIT_TENANT_LINE;

    private String account;
    private String filteredTenantLine;
    private String filteredCallingNumber;
    private EnableStatus enableStatus;
    private String maxConcurrency;
    private Set<String> secondIndustrySet;
    private List<DesignedSupplyLineGroup> supplyLineGroupList;

    public EditSingleTenantLineInstructionBean(String instructionId, ChatGroup chatGroup, String creator, String account, String filteredTenantLine, String filteredCallingNumber, EnableStatus enableStatus, String maxConcurrency, Set<String> secondIndustrySet, List<DesignedSupplyLineGroup> supplyLineGroupList) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.account = account;
        this.filteredTenantLine = filteredTenantLine;
        this.filteredCallingNumber = filteredCallingNumber;
        this.enableStatus = enableStatus;
        this.maxConcurrency = maxConcurrency;
        this.secondIndustrySet = secondIndustrySet;
        this.supplyLineGroupList = supplyLineGroupList;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();

        String tmpInstructionId = this.getInstructionId();
        if (StringUtils.isEmpty(tmpInstructionId)) {
            tmpInstructionId = "";
        }
        sb.append("指令ID：").append(tmpInstructionId).append("\n");

        sb.append("群名称：").append(this.getChatGroup().getName()).append("\n");

        String tmpCreator = this.getCreator();
        if (StringUtils.isEmpty(tmpCreator)) {
            tmpCreator = "";
        }
        sb.append("创建人：").append(tmpCreator).append("\n");

        sb.append("操作：").append(getInstructionType().getName().replace("操作类型_", "")).append("\n");

        sb.append(this.toDetailDescription());
        return sb.toString();
    }

    public String toDetailDescription() {
        StringBuilder sb = new StringBuilder();

        sb.append("账号：");
        if (!StringUtils.isEmpty(account)) {
            sb.append(account);
        }
        sb.append("\n");

        sb.append("商户线路：");
        if (!StringUtils.isEmpty(filteredTenantLine)) {
            sb.append(filteredTenantLine);
        }
        if (!StringUtils.isEmpty(filteredCallingNumber)) {
            sb.append(filteredCallingNumber);
        }
        sb.append("\n");

        if (enableStatus != null) {
            sb.append("线路状态：").append(enableStatus.getCaption()).append("\n");
        }

        if (!StringUtils.isEmpty(maxConcurrency)) {
            sb.append("并发上限：").append(maxConcurrency).append("\n");
        }

        if (!CollectionUtils.isEmpty(secondIndustrySet)) {
            sb.append("适用行业：").append(String.join("、", secondIndustrySet)).append("\n");
        }

        if (!CollectionUtils.isEmpty(supplyLineGroupList)) {
            for (DesignedSupplyLineGroup group : supplyLineGroupList) {
                String operatorName = group.getOperator().getCaption();
                for (DesignedDistrictSupplyLineInfo info : group.getDistrictSupplyLineInfoList()) {
                    List<DistrictBean> provinceList = info.getProvinceList();
                    List<DistrictBean> cityList = info.getCityList();
                    List<String> supplyLineNameList = info.getSupplyLineNameList();
                    sb.append("〖").append(operatorName).append("〗")
                            .append(Stream.concat(provinceList.stream(), cityList.stream()).map(x -> x.getName()).collect(Collectors.joining("、")))
                            .append("：")
                            .append(String.join(">", supplyLineNameList))
                            .append("\n");
                }
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
        if (StringUtils.isEmpty(getInstructionId())) {
            return new CheckResult(false,"指令ID为空");
        }
        if (getInstructionType() == null) {
            return new CheckResult(false,"指令类型为空");
        }
        if (StringUtils.isEmpty(account)) {
            return new CheckResult(false,"账号为空");
        }
        if (StringUtils.isEmpty(filteredTenantLine) && StringUtils.isEmpty(filteredCallingNumber)) {
            return new CheckResult(false,"线路名称为空");
        }
        if (enableStatus == null && StringUtils.isEmpty(maxConcurrency) && CollectionUtils.isEmpty(secondIndustrySet) && CollectionUtils.isEmpty(supplyLineGroupList)) {
            return new CheckResult(false,"线路更改的内容为空");
        }
        if (!StringUtils.isEmpty(maxConcurrency)) {
            try {
                if (Integer.parseInt(maxConcurrency) <= 0) {
                    return new CheckResult(false, "并发数必须为正整数");
                }
            } catch (Exception e) {
                return new CheckResult(false, "并发数必须为正整数");
            }
        }

        if (!CollectionUtils.isEmpty(supplyLineGroupList)) {
            Set<Operator> operatorSet = supplyLineGroupList.stream()
                    .map(group -> group.getOperator()).collect(Collectors.toSet());
            if (operatorSet.size() != supplyLineGroupList.size()) {
                return new CheckResult(false, "运营商不能有重复");
            }

            for (DesignedSupplyLineGroup group : supplyLineGroupList) {
                Operator operator = group.getOperator();
                if (operator == null) {
                    return new CheckResult(false, "运营商不能为空");
                }

                List<DesignedDistrictSupplyLineInfo> infoList = group.getDistrictSupplyLineInfoList();
                for (DesignedDistrictSupplyLineInfo info : infoList) {
                    List<DistrictBean> provinceList = info.getProvinceList();
                    List<DistrictBean> cityList = info.getCityList();
                    List<String> supplyLineNameList = info.getSupplyLineNameList();
                    if (CollectionUtils.isEmpty(provinceList) && CollectionUtils.isEmpty(cityList)) {
                        return new CheckResult(false, "运营商〖" + operator.getCaption() + "〗的地区不能为空");
                    }
                    if (CollectionUtils.isEmpty(supplyLineNameList)) {
                        return new CheckResult(false, "运营商〖" + operator.getCaption() + "〗的线路不能为空");
                    }
                }
            }
        }
        return new CheckResult(true, "");
    }

    @Override
    public CallableInfo getCallableInfo() {
        Callable<ExecutionResult> callable = () -> {
            try {
                CheckResult checkResult = this.checkValid();
                if (checkResult.isCorrect()) {
                    BaizeClient adminClient = BaizeClientFactory.getBaizeClient();

                    // 登录主账号
                    BaizeClient mainClient;
                    try {
                        mainClient = BaizeClientFactory.getBaizeClient(account);
                    } catch (Exception e) {
                        String msg = "指令执行失败！主账号【" + account + "】登录失败" + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }

                    String groupId = mainClient.getGroupId();
                    List<TenantLine> tenantLineList = adminClient.adminFindTenantLinesByConditions(null, groupId);
                    tenantLineList = tenantLineList.stream()
                            .filter(line -> InstructionUtils.isLine(line, filteredTenantLine, filteredCallingNumber))
                            .collect(Collectors.toList());

                    CheckResult lineCheckResult = InstructionUtils.checkTenantLineList(tenantLineList);
                    if (lineCheckResult.isError()) {
                        String msg = "执行指令失败！" + lineCheckResult.getMsg() + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }

                    TenantLine line = tenantLineList.get(0);

                    EnableStatus curEnableStatus = (enableStatus == null) ? line.getEnableStatus() : enableStatus;
                    int curMaxConcurrency = (StringUtils.isEmpty(maxConcurrency)) ? line.getConcurrentLimit() : Integer.parseInt(maxConcurrency);
                    Set<String> curSecondIndustrySet = (CollectionUtils.isEmpty(secondIndustrySet)) ? ImmutableSet.copyOf(line.getSecondIndustries()): secondIndustrySet;

                    List<SupplyLineGroup> newSupplyLineGroups;

                    if (CollectionUtils.isEmpty(supplyLineGroupList)) {
                        newSupplyLineGroups = line.getSupplyLineGroups();

                        if (!CollectionUtils.isAbsolutelyContain(curSecondIndustrySet, line.getSecondIndustries())) {
                            List<SupplyLine> allSupplyLineList = adminClient.adminFindSupplyLinesByConditions(null, null, curSecondIndustrySet, line.getLineType(), null, null, null, null, null, null, null);
                            for (SupplyLineGroup group : newSupplyLineGroups) {
                                List<String> supplyLineNoList = group.getSupplyLineNumbers().stream()
                                        .filter(lineNo -> allSupplyLineList.stream().anyMatch(supplyLine -> Objects.equals(supplyLine.getLineNumber(), lineNo)))
                                        .collect(Collectors.toList());
                                group.setSupplyLineNumbers(supplyLineNoList);
                            }
                        }
                    } else {
                        List<SupplyLine> allSupplyLineList = adminClient.adminFindSupplyLinesByConditions(null, null, curSecondIndustrySet, line.getLineType(), null, null, null, null, null, null, null);

                        newSupplyLineGroups = new ArrayList<>();
                        for (DesignedSupplyLineGroup designedGroup : supplyLineGroupList) {
                            Operator operator = designedGroup.getOperator();
                            List<DesignedDistrictSupplyLineInfo> designedInfoList = designedGroup.getDistrictSupplyLineInfoList();
                            for (DesignedDistrictSupplyLineInfo designedInfo : designedInfoList) {
                                List<DistrictBean> cityList = designedInfo.getCityList();
                                List<DistrictBean> provinceList = designedInfo.getProvinceList();
                                List<String> supplyLineNameList = designedInfo.getSupplyLineNameList();

                                Set<String> cityCodeSet = new HashSet<>();
                                if (!CollectionUtils.isEmpty(cityList)) {
                                    Set<String> tmpCityCodeSet = designedInfo.getCityList().stream().map(x -> x.getCode()).collect(Collectors.toSet());
                                    cityCodeSet.addAll(tmpCityCodeSet);
                                }
                                if (!CollectionUtils.isEmpty(provinceList)) {
                                    Set<String> tmpCityCodeSet = designedInfo.getProvinceList().stream()
                                            .flatMap(province -> DistrictUtils.getCityNameList(province.getName()).stream())
                                            .map(cityName -> DistrictUtils.getCityCode(cityName))
                                            .collect(Collectors.toSet());
                                    cityCodeSet.addAll(tmpCityCodeSet);
                                }

                                Set<String> supplyLineNameSet = new LinkedHashSet<>(supplyLineNameList);
                                List<String> supplyLineNoList = new ArrayList<>();

                                for (String supplyLineName : supplyLineNameSet) {
                                    String supplyLineNo = allSupplyLineList.stream()
                                            .filter(supplyLine -> Objects.equals(supplyLine.getLineName(), supplyLineName))
                                            .map(supplyLine -> supplyLine.getLineNumber())
                                            .findFirst().orElse(null);
                                    if (supplyLineNo == null) {
                                        String msg = "指令执行失败！供应线路【" + supplyLineName + "】不存在" + "\n" + this.toDescription();
                                        return new ExecutionResult(false, msg);
                                    } else {
                                        supplyLineNoList.add(supplyLineNo);
                                    }
                                }

                                SupplyLineGroup group = SupplyLineGroup.builder()
                                        .serviceProvider(operator)
                                        .cityCodes(ImmutableList.copyOf(cityCodeSet))
                                        .supplyLineNumbers(supplyLineNoList)
                                        .tenantLineNumber(line.getLineNumber())
                                        .build();
                                newSupplyLineGroups.add(group);
                            }
                        }
                    }

                    boolean success = adminClient.adminEditTenantLine(line, line.getLineName(), curEnableStatus, curMaxConcurrency, curSecondIndustrySet, newSupplyLineGroups, line.getNotes());
                    String msg;
                    if (success) {
                        msg = "执行指令成功！\n" + this.toDescription();
                    } else {
                        msg = "执行指令失败！\n" + this.toDescription();
                    }
                    return new ExecutionResult(success, msg);
                } else {
                    String msg = "执行指令失败！" + checkResult.getMsg() + "\n" + this.toDescription();
                    return new ExecutionResult(false, msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
                return new ExecutionResult(false, msg);
            }
        };
        return new CallableInfo(callable, 0);
    }
}
