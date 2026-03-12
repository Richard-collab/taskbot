package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.LinePairConcurrencyInfo;
import org.example.chat.bean.baize.SupplyLine;
import org.example.chat.bean.baize.TenantLine;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class EditSupplyLineInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_EDIT_SUPPLY_LINE;

    private String filteredLineNameContain;
    private String filteredCallingNumber;
    private String concurrency;

    public EditSupplyLineInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String filteredLineNameContain,
            String filteredCallingNumber, String concurrency) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.filteredLineNameContain = filteredLineNameContain;
        this.filteredCallingNumber = filteredCallingNumber;
        this.concurrency = concurrency;
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

        sb.append("针对线路：");
        if (!StringUtils.isEmpty(filteredLineNameContain)) {
            sb.append(filteredLineNameContain);
        }
        if (!StringUtils.isEmpty(filteredCallingNumber)) {
            sb.append(filteredCallingNumber);
        }
        sb.append("\n");

        sb.append("并发数：");
        if (!StringUtils.isEmpty(concurrency)) {
            sb.append(concurrency);
        }
        sb.append("\n");

        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
//        return new ArrayList<>(singleInstructionBeanList);
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
        if (StringUtils.isEmpty(filteredLineNameContain) && StringUtils.isEmpty(filteredCallingNumber)) {
            return new CheckResult(false,"针对的线路信息为空");
        }
        if (StringUtils.isEmpty(concurrency)) {
            return new CheckResult(false,"并发数为空");
        } else {
            try {
                if (Integer.parseInt(concurrency) <= 0) {
                    return new CheckResult(false, "并发数必须为正整数");
                }
            } catch (Exception e) {
                return new CheckResult(false, "并发数必须为正整数");
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
                    BaizeClient client = BaizeClientFactory.getBaizeClient();
                    List<SupplyLine> supplyLineList;
                    if (StringUtils.isEmpty(filteredLineNameContain)) {
                        // 线路名称为空
                        supplyLineList = client.adminFindSupplyLinesByConditions(null, null, null, null,null,null, null, null, null, null, filteredCallingNumber);
                    } else {
                        // 线路名称非空
                        supplyLineList = client.adminFindSupplyLinesByConditions(null, filteredLineNameContain, null, null,null,null, null, null, null, null, null);
                    }
                    if (CollectionUtils.isEmpty(supplyLineList)) {
                        supplyLineList = client.adminFindSupplyLinesByConditions(null, null, null, null,null,null, null, null, null, null, null);
                        supplyLineList = InstructionUtils.filterSupplyLine(supplyLineList, filteredLineNameContain, filteredCallingNumber);
                    }
                    if (CollectionUtils.isEmpty(supplyLineList)) {
                        String msg = "执行指令失败！没有合适的供应线路" + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                    if (supplyLineList.size() > 1) {
                        String msg = "执行指令失败！有多条供应线路可供选择：" + supplyLineList.stream().map(line -> line.getLineName()).collect(Collectors.joining("、")) + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                    SupplyLine supplyLine = supplyLineList.get(0);
                    int newConcurrency = Integer.parseInt(this.concurrency);
                    int oldConcurrency = supplyLine.getConcurrentLimit();
                    int addedConcurrency = newConcurrency - oldConcurrency;

                    List<TenantLine> tenantLineList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(supplyLine.getTenantLines())) {
                        for (TenantLine tenantLine: supplyLine.getTenantLines()) {
                            String tenantLineNumber = tenantLine.getLineNumber();
                            List<TenantLine> detailedTenantLineList = client.adminFindTenantLinesByConditions(tenantLineNumber, null);
                            if (CollectionUtils.isEmpty(detailedTenantLineList) || detailedTenantLineList.size() > 1) {
                                String msg = "执行指令失败！查询" + tenantLine.toDescription() + "出错" + "\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                            TenantLine detailedTenantLine = detailedTenantLineList.get(0);
                            if (detailedTenantLine.getConcurrentLimit() + addedConcurrency <= 0) {
                                String msg = "执行指令失败！" + tenantLine.toDescription() + "并发上限" + detailedTenantLine.getConcurrentLimit() + "减去" + (-addedConcurrency) + "后将小于等于0" + "\n" + this.toDescription();
                                return new ExecutionResult(false, msg);
                            }
                            tenantLineList.add(detailedTenantLine);
                        }
                    }

                    for (int i = 0; i < tenantLineList.size(); i++) {
                        TenantLine tenantLine = tenantLineList.get(i);
                        boolean success = client.adminEditTenantLine(tenantLine, tenantLine.getConcurrentLimit() + addedConcurrency);
                        if (success) {
                            List<LinePairConcurrencyInfo> linePairConcurrencyInfoList = Lists.newArrayList(new LinePairConcurrencyInfo(supplyLine.getLineNumber(), tenantLine.getLineNumber(), newConcurrency));
                            success = client.adminEditSupplyLineConcurrencyInTenantLine(linePairConcurrencyInfoList);
                        }
                        if (!success) {
                            String msg = "执行指令失败！"
                                    + tenantLine.toDescription() + "并发上限及供应线路在商户线路中的最大可支配并发修改出错\n"
                                    + "请手动修改以下商户线路：\n" + tenantLineList.subList(i, tenantLineList.size()).stream().map(x -> x.toDescription()).collect(Collectors.joining("\n"))
                                    + "并请手动修改供应线路【" + supplyLine.getLineName() + "】的并发上限"
                                    + "\n" + this.toDescription();
                            return new ExecutionResult(false, msg);
                        }

                    }
                    supplyLine.setConcurrentLimit(newConcurrency);
                    boolean success = client.adminEditSupplyLine(supplyLine);

                    String msg;
                    if (success) {
                        msg = "执行指令成功！\n" + this.toDescription();
                    } else {
                        msg = "执行指令失败！商户线路已设置成功，请手动设置供应线路\n" + this.toDescription();
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
            String msg = "执行指令失败！\n指令ID：" + getInstructionId() + "\n" + e + "\n" + this.toDescription();
            Callable<ExecutionResult> callable = () -> new ExecutionResult(false, msg);
            return new CallableInfo(callable, 0);
        }
    }
}
