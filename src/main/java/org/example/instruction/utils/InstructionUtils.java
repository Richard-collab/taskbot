package org.example.instruction.utils;

import org.example.chat.bean.baize.*;
import org.example.chat.bean.baize.script.Script;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.MysqlClient;
import org.example.instruction.bean.CheckResult;
import org.example.instruction.bean.TaskInfoBean;
import org.example.utils.CollectionUtils;
import org.example.utils.DatetimeUtils;
import org.example.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstructionUtils {

    private static boolean isTaskNameContain(AiTask task, String taskNameContain) {
        return Stream.of(taskNameContain.split("且"))
                .map(x -> x.trim())
                .filter(x -> !StringUtils.isEmpty(x))
                .allMatch(x -> task.getTaskName().contains(x));
    }

    public static List<AiTask> filterAiTask(
            List<AiTask> taskList, List<String> taskNameEqualList, List<String> taskNameContainList,
            List<String> taskNameNotContainList, List<String> taskNameSuffixList,
            String taskCreateTimeBoundStart, String taskCreateTimeBoundEnd) {
        String today = DatetimeUtils.getStrDate(new Date());
        if (!(CollectionUtils.isEmpty(taskNameEqualList)
                && CollectionUtils.isEmpty(taskNameContainList)
                && CollectionUtils.isEmpty(taskNameNotContainList)
                && CollectionUtils.isEmpty(taskNameSuffixList)
                && StringUtils.isEmpty(taskCreateTimeBoundStart)
                && StringUtils.isEmpty(taskCreateTimeBoundEnd))) {
            taskList = taskList.stream()
                    .filter(task -> (
                            (!CollectionUtils.isEmpty(taskNameEqualList) && taskNameEqualList.contains(task.getTaskName()))
                                    || (
                                    (CollectionUtils.isEmpty(taskNameContainList) || taskNameContainList.stream().anyMatch(x -> isTaskNameContain(task, x)))
                                            && (CollectionUtils.isEmpty(taskNameNotContainList) || taskNameNotContainList.stream().noneMatch(x -> isTaskNameContain(task, x)))
                                            && (CollectionUtils.isEmpty(taskNameSuffixList) || taskNameSuffixList.stream().anyMatch(x -> task.getTaskName().endsWith(x)))
                                            && !(CollectionUtils.isEmpty(taskNameContainList) && CollectionUtils.isEmpty(taskNameNotContainList) && CollectionUtils.isEmpty(taskNameSuffixList) && !CollectionUtils.isEmpty(taskNameEqualList))
                            ) && (
                                    (StringUtils.isEmpty(taskCreateTimeBoundStart) || task.getCreateTime().compareTo(today + "T" + taskCreateTimeBoundStart) >= 0)
                                    && (StringUtils.isEmpty(taskCreateTimeBoundEnd) || task.getCreateTime().compareTo(today + "T" + taskCreateTimeBoundEnd) <= 0)
                            )
                        )
                    )
                    .collect(Collectors.toList());
        }
        return taskList;
    }


    public static List<AiTask> filterAiTask(List<AiTask> taskList, Collection<TaskStatus> taskStatusList) {
        if (!(CollectionUtils.isEmpty(taskStatusList))) {
            taskList = taskList.stream()
                    .filter(task -> taskStatusList.stream().anyMatch(taskStatus -> Objects.equals(taskStatus.getCaption(), task.getCallStatus())))
                    .collect(Collectors.toList());
        }
        return taskList;
    }

    public static List<AiTask> filterAiTask(List<AiTask> taskList, TaskType taskType) {
        if (taskType != null) {
            taskList = taskList.stream()
                    .filter(task -> task.getTaskType() == taskType)
                    .collect(Collectors.toList());
        }
        return taskList;
    }

    public static List<AiTask> filterAiTaskByTaskType(List<AiTask> taskList, List<TaskType> taskTypeList) {
        if (!(CollectionUtils.isEmpty(taskTypeList))) {
            taskList = taskList.stream()
                    .filter(task -> taskTypeList.contains(task.getTaskType()))
                    .collect(Collectors.toList());
        }
        return taskList;
    }

    public static List<AiTask> filterAiTaskById(List<AiTask> taskList, Collection<Long> taskIds) {
        if (!CollectionUtils.isEmpty(taskIds)) {
            return taskList.stream()
                    .filter(task -> taskIds.contains(task.getId()))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public static List<AiTask> filterAiTaskByTenantLineName(List<AiTask> taskList, String tenantLineNameContain, String callingNumber) {
        if (!StringUtils.isEmpty(tenantLineNameContain) || !StringUtils.isEmpty(callingNumber)) {
            taskList = taskList.stream()
                    .filter(task -> isAiTask(task, tenantLineNameContain, callingNumber))
                    .collect(Collectors.toList());
        }
        return taskList;
    }

    public static List<AiTask> filterUnlockedAiTask(List<AiTask> taskList) {
        return taskList.stream()
                .filter(task -> !Objects.equals(task.getIfLock(), 1))
                .collect(Collectors.toList());
    }

    public static boolean isAiTask(AiTask task, String tenantLineNameContain, String callingNumberContain) {
        return !StringUtils.isEmpty(task.getLineName()) && InstructionUtils.isLine(task.getLineName(), tenantLineNameContain, callingNumberContain);
    }

    public static List<String> getLackTaskNameEqualList(List<String> taskNameEqualList, List<AiTask> taskList) {
        if (CollectionUtils.isEmpty(taskNameEqualList)) {
            return taskNameEqualList;
        } else {
            return taskNameEqualList.stream()
                    .filter(taskNameEqual -> taskList.stream().noneMatch(task -> Objects.equals(task.getTaskName(), taskNameEqual)))
                    .collect(Collectors.toList());
        }
    }

    public static boolean isLine(String lineName, String lineNameContain, String callingNumberContain) {
        return (StringUtils.isEmpty(lineNameContain) || lineName.toLowerCase().contains(lineNameContain.toLowerCase()))
                && (StringUtils.isEmpty(callingNumberContain) || lineName.toLowerCase().contains(callingNumberContain.toLowerCase()));
    }

    public static boolean isLine(SupplyLine line, String lineNameContain, String callingNumberContain) {
        String supplyLineName = line.getLineName();
        return isLine(supplyLineName, lineNameContain, callingNumberContain);
    }

    public static boolean isLine(TenantLine line, String lineNameContain, String callingNumberContain) {
        String tenantLineName = line.getLineName();
        return isLine(tenantLineName, lineNameContain, callingNumberContain);
    }

    public static List<SupplyLine> filterSupplyLine(
            List<SupplyLine> supplyLineList, String lineNameContain, String callingNumberContain) {
        if (!StringUtils.isEmpty(lineNameContain) || !StringUtils.isEmpty(callingNumberContain)) {
            supplyLineList = supplyLineList.stream()
                    .filter(line -> isLine(line, lineNameContain, callingNumberContain) || (line.getCallLineSupplierName() != null && line.getCallLineSupplierName().contains(lineNameContain)))
                    .collect(Collectors.toList());
        }
        return supplyLineList;
    }

    public static List<TenantLine> filterTenantLine(
            List<TenantLine> tenantLineList, String lineNameContain, String callingNumberContain) {
        if (!StringUtils.isEmpty(lineNameContain) || !StringUtils.isEmpty(callingNumberContain)) {
            tenantLineList = tenantLineList.stream()
                    .filter(line -> isLine(line, lineNameContain, callingNumberContain))
                    .collect(Collectors.toList());
        }
        return tenantLineList;
    }

    public static List<TaskTemplate> filterTaskTemplate(List<TaskTemplate> taskTemplateList, String scriptName) {
        if (!CollectionUtils.isEmpty(taskTemplateList)) {
            taskTemplateList = taskTemplateList.stream()
                    .filter(taskTemplate -> Objects.equals(taskTemplate.getSpeechCraftName(), scriptName))
                    .collect(Collectors.toList());
        }
        return taskTemplateList;
    }

    public static Script filterScript(List<Script> scriptList, String scriptName) {
        if (!CollectionUtils.isEmpty(scriptList) && !StringUtils.isEmpty(scriptName)) {
            return scriptList.stream()
                    .filter(script -> isScript(script, scriptName))
                    .findFirst().orElse(null);
        } else {
            return null;
        }
    }

    public static List<Script> fuzzyFilterScript(List<Script> scriptList, String scriptName) {
        if (!CollectionUtils.isEmpty(scriptList) && !StringUtils.isEmpty(scriptName)) {
            return scriptList.stream()
                    .filter(script -> isFuzzyScript(script, scriptName))
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public static boolean isScript(Script script, String scriptName) {
        if (script == null) {
            return false;
        }
        return Objects.equals(script.getScriptName(), scriptName);
    }

    public static boolean isFuzzyScript(Script script, String scriptName) {
        if (script == null) {
            return false;
        }
        if (Objects.equals(script.getScriptName(), scriptName)) {
            return true;
        }
        if (StringUtils.isEmpty(script.getScriptName()) || StringUtils.isEmpty(scriptName)) {
            return false;
        }
        return StringUtils.isSubsequence(scriptName, script.getScriptName());
    }

    public static CheckResult checkTenantLineList(List<TenantLine> tenantLineList) {
        // 合法性检查，线路只能有一个，任务列表不能为空
        boolean isCorrect = true;
        String errorMsg = null;
        if (CollectionUtils.isEmpty(tenantLineList)) {
            isCorrect = false;
            errorMsg = "没有合适的商户线路";
        }
        if (tenantLineList.size() > 1) {
            isCorrect = false;
            errorMsg = "有多条商户线路可供选择：" + tenantLineList.stream().map(line -> line.getLineName()).collect(Collectors.joining("、"));
        }
        return new CheckResult(isCorrect, errorMsg);
    }

    public static long getDelaySec(String strExpectedStartTime) {
        long delaySec = 0;
        if (!StringUtils.isEmpty(strExpectedStartTime)) {
            Date date = new Date();
            Date expectedStartTime = DatetimeUtils.getDatetime(DatetimeUtils.getStrDate(date) + " " + strExpectedStartTime + ":00");
            if (date.before(expectedStartTime)) {
                delaySec = DatetimeUtils.getDiffMs(date, expectedStartTime) / 1000;
            }
        }
        return delaySec;
    }

    public static List<TenantLine> getTenantLineList(
            BaizeClient client, TaskType taskType, Long scriptId, String tenantLineName, String callingNumber) {
        List<TenantLine> tenantLineList = new ArrayList<>();
        // 需要根据话术获得行业，根据行业查找商户线路，暂取该账号的第一个任务的话术
        LineType lineType = taskType.getLineType();
        if (scriptId != null) {
            tenantLineList = client.findActiveTenantLinesByGroupId(scriptId, lineType);
        }
        if (taskType == TaskType.AI_AUTO) {
            tenantLineList = tenantLineList.stream()
                    .filter(tenantLine -> !tenantLine.getLineName().contains("转人工"))
                    .collect(Collectors.toList());
        } else if (taskType == TaskType.AI_MANUAL) {
            tenantLineList = tenantLineList.stream()
                    .filter(tenantLine -> tenantLine.getLineName().contains("转人工"))
                    .collect(Collectors.toList());
        }
        if (!StringUtils.isEmpty(tenantLineName) || !StringUtils.isEmpty(callingNumber)) {
            tenantLineList = InstructionUtils.filterTenantLine(tenantLineList, tenantLineName, callingNumber);
        }
        return tenantLineList;
    }

    public static Set<String> getYesterdayScriptNameSet(String account) {
        // 查询单个用户
//        String sql = "SELECT distinct `话术名称` as script_name FROM grc_script_details_5 WHERE `日期` = ? and `账号` = ?";
        String strDate = DatetimeUtils.getStrDate(DatetimeUtils.addDay(new Date(), -1));
//        List<String> scriptNameList = MysqlClient.queryList(sql, rs -> rs.getString("script_name"), strDate, account);
        Set<String> scriptNameSet = BaizeClientFactory.getBaizeClient().adminGetScriptNamesByDateAndAccount(strDate, account);

        return scriptNameSet;
    }
}
