package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.AiTask;
import org.example.chat.utils.*;
import org.example.instruction.utils.DistrictUtils;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ForbidDistrictInstructionBean extends AbstractInstructionBean {

    private String account;
    private String product;
    private String entranceMode;
    // 任务名称
    private List<String> taskNameEqualList;
    // 任务名称包含
    private List<String> taskNameContainList;
    // 任务名称不包含
    private List<String> taskNameNotContainList;
    // 任务名称后缀
    private List<String> taskNameSuffixList;
    // 任务创建时间上限
    private String taskCreateTimeBoundStart;
    // 任务创建时间下限
    private String taskCreateTimeBoundEnd;
    private Set<DistrictBean> forbiddenProvinceSet;
    private Set<DistrictBean> forbiddenCitySet;
    private boolean effectiveAllDay;
    private Set<Long> filteredTaskIdSet;

    public ForbidDistrictInstructionBean(
            String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, String account, String product,
            String entranceMode, List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList,
            List<String> taskNameSuffixList, String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd,
            Set<DistrictBean> forbiddenProvinceSet, Set<DistrictBean> forbiddenCitySet, boolean effectiveAllDay) {
        super(instructionId, instructionType, chatGroup, creator);
        this.account = account;
        this.product = product;
        this.entranceMode = entranceMode;
        this.taskNameEqualList = taskNameEqualList;
        this.taskNameContainList = taskNameContainList;
        this.taskNameNotContainList = taskNameNotContainList;
        this.taskNameSuffixList = taskNameSuffixList;
        this.taskCreateTimeBoundStart = taskCreateTimeBoundStart;
        this.taskCreateTimeBoundEnd = taskCreateTimeBoundEnd;
        this.forbiddenProvinceSet = forbiddenProvinceSet;
        this.forbiddenCitySet = forbiddenCitySet;
        this.effectiveAllDay = effectiveAllDay;
        this.filteredTaskIdSet = Collections.emptySet();
    }

    public ForbidDistrictInstructionBean(
            String instructionId, InstructionType instructionType, ChatGroup chatGroup, String creator, String account, String product,
            String entranceMode, List<String> taskNameEqualList, List<String> taskNameContainList, List<String> taskNameNotContainList,
            List<String> taskNameSuffixList, String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd,
            Set<DistrictBean> forbiddenProvinceSet, Set<DistrictBean> forbiddenCitySet, boolean effectiveAllDay, Set<Long> filteredTaskIdSet) {
        super(instructionId, instructionType, chatGroup, creator);
        this.account = account;
        this.product = product;
        this.entranceMode = entranceMode;
        this.taskNameEqualList = taskNameEqualList;
        this.taskNameContainList = taskNameContainList;
        this.taskNameNotContainList = taskNameNotContainList;
        this.taskNameSuffixList = taskNameSuffixList;
        this.taskCreateTimeBoundStart = taskCreateTimeBoundStart;
        this.taskCreateTimeBoundEnd = taskCreateTimeBoundEnd;
        this.forbiddenProvinceSet = forbiddenProvinceSet;
        this.forbiddenCitySet = forbiddenCitySet;
        this.effectiveAllDay = effectiveAllDay;
        this.filteredTaskIdSet = filteredTaskIdSet;
    }

    public String getAccount() {
        return account;
    }

    public String getProduct() {
        return product;
    }

    public String getEntranceMode() {
        return entranceMode;
    }

    public List<String> getTaskNameEqualList() {
        return taskNameEqualList;
    }

    public List<String> getTaskNameContainList() {
        return taskNameContainList;
    }

    public List<String> getTaskNameNotContainList() {
        return taskNameNotContainList;
    }

    public List<String> getTaskNameSuffixList() {
        return taskNameSuffixList;
    }

    public Set<DistrictBean> getForbiddenProvinceSet() {
        return forbiddenProvinceSet;
    }

    public Set<DistrictBean> getForbiddenCitySet() {
        return forbiddenCitySet;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();

        String tmpInstructionId = getInstructionId();
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

        String tmpAccount = account;
        if (StringUtils.isEmpty(tmpAccount)) {
            tmpAccount = "";
        }
        sb.append("账号：").append(tmpAccount).append("\n");

        String tmpProduct = product;
        if (StringUtils.isEmpty(tmpProduct)) {
            tmpProduct = "";
        }
        String tmpEntranceMode = entranceMode;
        if (StringUtils.isEmpty(tmpEntranceMode)) {
            tmpEntranceMode = "";
        }
        sb.append("业务：").append(tmpProduct).append(tmpEntranceMode).append("\n");

        sb.append("全天生效：").append(effectiveAllDay? "是": "否").append("\n");

        if (!CollectionUtils.isEmpty(taskNameContainList)
                || !CollectionUtils.isEmpty(taskNameNotContainList)
                || !CollectionUtils.isEmpty(taskNameSuffixList)
                || !StringUtils.isEmpty(taskCreateTimeBoundStart)
                || !StringUtils.isEmpty(taskCreateTimeBoundEnd)) {
            if (!CollectionUtils.isEmpty(taskNameContainList)) {
                sb.append("包含").append("{" + String.join("}、{", taskNameContainList) + "}").append("的任务").append(" ");
            }
            if (!CollectionUtils.isEmpty(taskNameNotContainList)) {
                sb.append("不包含").append("{" + String.join("}、{", taskNameNotContainList) + "}").append("的任务").append(" ");
            }
            if (!CollectionUtils.isEmpty(taskNameSuffixList)) {
                sb.append("以").append("{" + String.join("}、{", taskNameSuffixList) + "}").append("结尾的任务").append(" ");
            }
            if (!StringUtils.isEmpty(taskCreateTimeBoundStart) || !StringUtils.isEmpty(taskCreateTimeBoundEnd)) {
                sb.append("任务创建时间在").append(taskCreateTimeBoundStart).append("到") .append(taskCreateTimeBoundEnd).append("之间").append(" ");
            }
            sb.append("\n");
        }

        if (!CollectionUtils.isEmpty(taskNameEqualList)) {
            sb.append("任务列表：【\n")
                    .append(String.join("\n", taskNameEqualList))
                    .append("\n】");
            sb.append("\n");
        }

        List<String> forbiddenPlaceList = new ArrayList<>();
        for (DistrictBean districtBean: forbiddenProvinceSet) {
            forbiddenPlaceList.add(districtBean.getName());
        }
        for (DistrictBean districtBean: forbiddenCitySet) {
            forbiddenPlaceList.add(districtBean.getName());
        }
        if (InstructionType.ACTION_ALLOW_DISTRICT == getInstructionType()) {
            sb.append("放开");
        }
        sb.append("屏蔽：").append(String.join("、", forbiddenPlaceList));

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
        if (this.getInstructionType() == null) {
            return new CheckResult(false,"指令类型为空");
        }
        if (StringUtils.isEmpty(account)) {
            return new CheckResult(false,"账号为空");
        }
        if (CollectionUtils.isEmpty(forbiddenCitySet) && CollectionUtils.isEmpty(forbiddenProvinceSet)) {
            return new CheckResult(false,"缺少省份或城市信息");
        }
        if (effectiveAllDay && (!CollectionUtils.isEmpty(taskNameEqualList) || !CollectionUtils.isEmpty(taskNameContainList) || !CollectionUtils.isEmpty(taskNameNotContainList) || !CollectionUtils.isEmpty(taskNameSuffixList))) {
            return new CheckResult(false,"全天生效的屏蔽指令不能指定任务");
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
                    BaizeClient client = BaizeClientFactory.getBaizeClient(account);

                    BaizeClient.ChangeType changeType = null;
                    switch (getInstructionType()) {
                        case ACTION_FORBID_DISTRICT: {
                            changeType = BaizeClient.ChangeType.ADD;
                            if (effectiveAllDay) {
                                ForbiddenDistrictUtils.add(account, forbiddenProvinceSet, forbiddenCitySet);
                            }
                            break;
                        }
                        case ACTION_ALLOW_DISTRICT: {
                            changeType = BaizeClient.ChangeType.REMOVE;
                            if (effectiveAllDay) {
                                ForbiddenDistrictUtils.remove(account, forbiddenProvinceSet, forbiddenCitySet);
                            }
                            break;
                        }
                        default: {
                            break;
                        }
                    }

                    List<AiTask> taskList = client.findAiOutboundTasks();
                    taskList = InstructionUtils.filterUnlockedAiTask(taskList);
                    taskList = InstructionUtils.filterAiTask(taskList, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd);
                    if (!CollectionUtils.isEmpty(filteredTaskIdSet)) {
                        taskList = InstructionUtils.filterAiTaskById(taskList, filteredTaskIdSet);
                    }
                    if (CollectionUtils.isEmpty(taskList)) {
                        return new ExecutionResult(false, "执行指令失败！未找到符合条件的任务\n" + this.toDescription());
                    }
                    List<String> lackTaskNameEqualList = InstructionUtils.getLackTaskNameEqualList(taskNameEqualList, taskList);
                    if (!CollectionUtils.isEmpty(lackTaskNameEqualList)) {
                        String msg = "执行指令失败！未找到符合条件的任务：\n" + String.join("\n", lackTaskNameEqualList) + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    }
                    Set<Long> taskIdSet = taskList.stream().map(task -> task.getId()).collect(Collectors.toSet());
                    Set<String> allProvinceCodeSet = forbiddenProvinceSet.stream().map(province -> province.getCode()).collect(Collectors.toCollection(HashSet::new));
                    Set<String> allCityCodeSet = forbiddenCitySet.stream().map(city -> city.getCode()).collect(Collectors.toCollection(HashSet::new));
                    Set<String> provinceCodeSet = forbiddenCitySet.stream()
                            .map(city -> DistrictUtils.getProvinceCode(DistrictUtils.getProvinceName(city.getName())))
                            .collect(Collectors.toSet());
                    allProvinceCodeSet.addAll(provinceCodeSet);
                    Set<String> cityCodeSet = forbiddenProvinceSet.stream()
                            .flatMap(province -> DistrictUtils.getCityNameList(province.getName()).stream())
                            .map(cityName -> DistrictUtils.getCityCode(cityName))
                            .collect(Collectors.toSet());
                    allCityCodeSet.addAll(cityCodeSet);

                    if (changeType != null) {
                        client.batchPreProcess(taskIdSet);
                        boolean success = client.changeRestrictArea(changeType, taskIdSet, allProvinceCodeSet, allCityCodeSet,
                                null, null, null, null, null, null, null, null, null, null);
                        result = new ExecutionResult(success, getMsg(success, taskIdSet.size()));
                    } else {
                        String msg = "执行指令失败！不支持该类型任务\n" + this.toDescription();
                        result = new ExecutionResult(false, msg);
                    }

                } else {
                    String msg = "执行指令失败！" + checkResult.getMsg() + "\n" + this.toDescription();
                    result = new ExecutionResult(false, msg);
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
