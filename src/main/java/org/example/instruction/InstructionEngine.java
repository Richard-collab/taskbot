package org.example.instruction;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import org.example.chat.bean.baize.*;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.customtenantline.CustomSupplyLineGroup;
import org.example.chat.bean.baize.customtenantline.CustomTenantLineInfo;
import org.example.chat.bean.baize.designedtenantline.DesignedSupplyLineGroup;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.ExecuteInstructionUtils;
import org.example.preporcess.Preprocessor;
import org.example.ruledetect.*;
import org.example.instruction.bean.*;
import org.example.instruction.utils.DistrictUtils;
import org.example.ruledetect.bean.*;
import org.example.ruledetect.slotrule.AbstractSlotRule;
import org.example.ruledetect.slotrule.WhitelistSlotRule;
import org.example.utils.*;

import java.util.*;
import java.util.stream.Collectors;

public class InstructionEngine {

    private static final String STR_INSTRUCTION_ID = ConstUtils.STR_INSTRUCTION_ID;
    private static final String STR_EXECUTE_INSTRUCTION_ID = ConstUtils.STR_EXECUTE_INSTRUCTION_ID;
    private static final String STR_ACCOUNT = ConstUtils.STR_ACCOUNT;
    private static final String STR_TENANT_LINE = ConstUtils.STR_TENANT_LINE;
    private static final String STR_CALLING_NUMBER = ConstUtils.STR_CALL_NUMBER;
    private static final String STR_FILTERED_TENANT_LINE = ConstUtils.STR_FILTERED_TENANT_LINE;
    private static final String STR_FILTERED_CALLING_NUMBER = ConstUtils.STR_FILTERED_CALLING_NUMBER;
    private static final String STR_CONCURRENCY = ConstUtils.STR_CONCURRENCY;
    private static final String STR_TASK_CREATE_TIME_BOUND_PERIOD = ConstUtils.STR_TASK_CREATE_TIME_BOUND_PERIOD;
    private static final String STR_TASK_CREATE_TIME_BOUND_START = ConstUtils.STR_TASK_CREATE_TIME_BOUND_START;
    private static final String STR_TASK_CREATE_TIME_BOUND_END = ConstUtils.STR_TASK_CREATE_TIME_BOUND_END;
    private static final String STR_START_DATE = ConstUtils.STR_START_DATE;
    private static final String STR_END_DATE = ConstUtils.STR_END_DATE;
    private static final String STR_EXPECTED_START_TIME = ConstUtils.STR_EXPECTED_START_TIME;
    private static final String STR_EXPECTED_END_TIME = ConstUtils.STR_EXPECTED_END_TIME;
    private static final String STR_EXPECTED_CONNECTED_CALL_COUNT = ConstUtils.STR_EXPECTED_CONNECTED_CALL_COUNT;
    private static final String STR_TASK_NAME_LIST = ConstUtils.STR_TASK_NAME_LIST;
    private static final String STR_TASK_NAME_EQUAL = ConstUtils.STR_TASK_NAME_EQUAL;
    private static final String STR_TASK_NAME_CONTAIN = ConstUtils.STR_TASK_NAME_CONTAIN;
    private static final String STR_TASK_NAME_NOT_CONTAIN = ConstUtils.STR_TASK_NAME_NOT_CONTAIN;
    private static final String STR_TASK_NAME_SUFFIX = ConstUtils.STR_TASK_NAME_SUFFIX;
    private static final String STR_TASK_TYPE = ConstUtils.STR_TASK_TYPE;
    private static final String STR_TASK_STATUS = ConstUtils.STR_TASK_STATUS;
    private static final String STR_CITY = ConstUtils.STR_CITY;
    private static final String STR_PROVINCE = ConstUtils.STR_PROVINCE;
    private static final String STR_FORBIDDEN_CITY = ConstUtils.STR_FORBIDDEN_CITY;
    private static final String STR_FORBIDDEN_PROVINCE = ConstUtils.STR_FORBIDDEN_PROVINCE;
    private static final String STR_EFFECTIVE_ALL_DAY = ConstUtils.STR_EFFECTIVE_ALL_DAY;
    private static final String STR_ORIGINAL_CONCURRENCY = ConstUtils.STR_ORIGINAL_CONCURRENCY;
    private static final String STR_PRODUCT = ConstUtils.STR_PRODUCT;
    private static final String STR_ENTRANCE_MODE = ConstUtils.STR_ENTRANCE_MODE;
    private static final String STR_ACCOUNT_SUFFIX = ConstUtils.STR_ACCOUNT_SUFFIX;
    private static final String STR_LINE_RATIO = ConstUtils.STR_LINE_RATIO;
    private static final String STR_PASSWORD = ConstUtils.STR_PASSWORD;
    private static final String STR_TENANT_NAME = ConstUtils.STR_TENANT_NAME;
    private static final String STR_CONTACTS = ConstUtils.STR_CONTACTS;
    private static final String STR_ROLE_NAME = ConstUtils.STR_ROLE_NAME;
    private static final String STR_SOURCE_ROLE_NAME = ConstUtils.STR_SOURCE_ROLE_NAME;
    private static final String STR_PHONE_NUM_TYPE = ConstUtils.STR_PHONE_NUM_TYPE;
    private static final String STR_SECOND_INDUSTRY_NAME = ConstUtils.STR_SECOND_INDUSTRY_NAME;
    private static final String STR_IP = ConstUtils.STR_IP;
    private static final String STR_IP_LIST = ConstUtils.STR_IP_LIST;
    private static final String STR_BAIZE_IP = ConstUtils.STR_BAIZE_IP;
    private static final String STR_BAIZE_IP_LIST = ConstUtils.STR_BAIZE_IP_LIST;
    private static final String STR_JIAFANG_IP = ConstUtils.STR_JIAFANG_IP;
    private static final String STR_JIAFANG_IP_LIST = ConstUtils.STR_JIAFANG_IP_LIST;
    private static final String STR_ACCOUNT_INFO = ConstUtils.STR_ACCOUNT_INFO;
    private static final String STR_ACCOUNT_INFO_LIST = ConstUtils.STR_ACCOUNT_INFO_LIST;
//    private static final String STR_BAIZE_YUNYING_ACCOUNT_INFO = ConstUtils.STR_BAIZE_YUNYING_ACCOUNT_INFO;
//    private static final String STR_BAIZE_YUNYING_ACCOUNT_INFO_LIST = ConstUtils.STR_BAIZE_YUNYING_ACCOUNT_INFO_LIST;
//    private static final String STR_JIAFANG_GUANLIYUAN_ACCOUNT_INFO = ConstUtils.STR_JIAFANG_GUANLIYUAN_ACCOUNT_INFO;
//    private static final String STR_JIAFANG_GUANLIYUAN_ACCOUNT_INFO_LIST = ConstUtils.STR_JIAFANG_GUANLIYUAN_ACCOUNT_INFO_LIST;
//    private static final String STR_WAIBU_ZUOXIZUZHANG_ACCOUNT_INFO = ConstUtils.STR_WAIBU_ZUOXIZUZHANG_ACCOUNT_INFO;
//    private static final String STR_WAIBU_ZUOXIZUZHANG_ACCOUNT_INFO_LIST = ConstUtils.STR_WAIBU_ZUOXIZUZHANG_ACCOUNT_INFO_LIST;
//    private static final String STR_WAIBU_ZUOXI_ACCOUNT_INFO = ConstUtils.STR_WAIBU_ZUOXI_ACCOUNT_INFO;
//    private static final String STR_WAIBU_ZUOXI_ACCOUNT_INFO_LIST = ConstUtils.STR_WAIBU_ZUOXI_ACCOUNT_INFO_LIST;
    private static final String STR_DATA_STATISTIC_OUTBOUND_TYPE_LIST = ConstUtils.STR_DATA_STATISTIC_OUTBOUND_TYPE_LIST;
    private static final String STR_DATA_STATISTIC_OUTBOUND_TYPE = ConstUtils.STR_DATA_STATISTIC_OUTBOUND_TYPE;
    private static final String STR_TASK_CALLBACK_URL = ConstUtils.STR_TASK_CALLBACK_URL;
    private static final String STR_CALLBACK_OUTBOUND_TYPE_LIST = ConstUtils.STR_CALLBACK_OUTBOUND_TYPE_LIST;
    private static final String STR_CALLBACK_OUTBOUND_TYPE = ConstUtils.STR_CALLBACK_OUTBOUND_TYPE;
    private static final String STR_CALLBACK_STATUS_LIST = ConstUtils.STR_CALLBACK_STATUS_LIST;
    private static final String STR_CALLBACK_STATUS = ConstUtils.STR_CALLBACK_STATUS;
    private static final String STR_OUTBOUND_CALLBACK_FIELD_LIST = ConstUtils.STR_OUTBOUND_CALLBACK_FIELD_LIST;
    private static final String STR_OUTBOUND_CALLBACK_FIELD = ConstUtils.STR_OUTBOUND_CALLBACK_FIELD;
    private static final String STR_QUICKLY_CALLBACK_URL = ConstUtils.STR_QUICKLY_CALLBACK_URL;
    private static final String STR_NEW_CALLBACK_URL = ConstUtils.STR_NEW_CALLBACK_URL;
    private static final String STR_TXT_UPDATE_CALLBACK_URL = ConstUtils.STR_TXT_UPDATE_CALLBACK_URL;
    private static final String STR_M_SMS_CALLBACK_URL = ConstUtils.STR_M_SMS_CALLBACK_URL;
    private static final String STR_OLD_CALLBACK_URL = ConstUtils.STR_OLD_CALLBACK_URL;
    private static final String STR_SMS_CALLBACK_FIELD_LIST = ConstUtils.STR_SMS_CALLBACK_FIELD_LIST;
    private static final String STR_SMS_CALLBACK_FIELD = ConstUtils.STR_SMS_CALLBACK_FIELD;
    private static final String STR_SMS_CALLBACK_URL = ConstUtils.STR_SMS_CALLBACK_URL;
    private static final String STR_UP_SMS_CALLBACK_URL = ConstUtils.STR_UP_SMS_CALLBACK_URL;
    private static final String STR_TEMPLATE_ID = ConstUtils.STR_TEMPLATE_ID;
    private static final String STR_TEMPLATE_NAME = ConstUtils.STR_TEMPLATE_NAME;
    private static final String STR_INTENTION_CLASS = ConstUtils.STR_INTENTION_CLASS;
    private static final String STR_INTENTION_CLASS_LIST = ConstUtils.STR_INTENTION_CLASS_LIST;
    private static final String STR_IS_OUTPUT_AVG_CALL_DURATION = ConstUtils.STR_IS_OUTPUT_AVG_CALL_DURATION;
    private static final String STR_IS_OUTPUT_TOTAL_CALL_DURATION = ConstUtils.STR_IS_OUTPUT_TOTAL_CALL_DURATION;
    private static final String STR_IS_NEXT_DAY_CALL = ConstUtils.STR_IS_NEXT_DAY_CALL;
    private static final String STR_IS_INCLUDE_AUTO_STOP = ConstUtils.STR_IS_INCLUDE_AUTO_STOP;
    private static final String STR_SCRIPT_NAME_LIST = ConstUtils.STR_SCRIPT_NAME_LIST;
    private static final String STR_SCRIPT_NAME = ConstUtils.STR_SCRIPT_NAME;
    private static final String STR_CALL_TEAM_NAME = ConstUtils.STR_CALL_TEAM_NAME;
    private static final String STR_CALL_TEAM_NAME_LIST = ConstUtils.STR_CALL_TEAM_NAME_LIST;
    private static final String STR_HANDLE_TYPE = ConstUtils.STR_HANDLE_TYPE;
    private static final String STR_OCCUPY_RATE = ConstUtils.STR_OCCUPY_RATE;
    private static final String STR_VIRTUAL_SEAT_RATIO = ConstUtils.STR_VIRTUAL_SEAT_RATIO;
    private static final String STR_REPLACE_TYPE = ConstUtils.STR_REPLACE_TYPE;
    private static final String STR_OLD_PHRASE = ConstUtils.STR_OLD_PHRASE;
    private static final String STR_NEW_PHRASE = ConstUtils.STR_NEW_PHRASE;
    private static final String STR_CONTENT_CONTAIN = ConstUtils.STR_CONTENT_CONTAIN;
    private static final String STR_INTENT = ConstUtils.STR_INTENT;
    private static final String STR_INTENT_LIST = ConstUtils.STR_INTENT_LIST;

    private static final Set<String> STR_YES_SET = Sets.newHashSet("是", "开");
    private static final Set<String> STR_NO_SET = Sets.newHashSet("否", "关");
    private static final String STR_PHONE = ConstUtils.STR_PHONE;
    private static final String STR_DATE = ConstUtils.STR_DATE;
    private static final String STR_YEAR = ConstUtils.STR_YEAR;
    private static final String STR_MONTH = ConstUtils.STR_MONTH;
    private static final String STR_DAY = ConstUtils.STR_DAY;
    private static final String STR_HOUR = ConstUtils.STR_HOUR;
    private static final String STR_MINUTE = ConstUtils.STR_MINUTE;
    private static final String STR_TIME = ConstUtils.STR_TIME;
    private static final String STR_MORNING = ConstUtils.STR_MORNING;
    private static final String STR_AFTERNOON = ConstUtils.STR_AFTERNOON;

    private static final String STR_ACTION_EXECUTE_INSTRUCTION = InstructionType.ACTION_EXECUTE_INSTRUCTION.getName();
    private static final String STR_ACTION_REMOVE_INSTRUCTION = InstructionType.ACTION_REMOVE_INSTRUCTION.getName();
    private static final String STR_ACTION_REPORT_INSTRUCTION = InstructionType.ACTION_REPORT_INSTRUCTION.getName();
    private static final String STR_ACTION_REPORT_FINISHED_INSTRUCTION = InstructionType.ACTION_REPORT_FINISHED_INSTRUCTION.getName();
    private static final String STR_ACTION_REPORT_FORBID_DISTRICT_ALL_DAY = InstructionType.ACTION_REPORT_FORBID_DISTRICT_ALL_DAY.getName();
    private static final String STR_ACTION_CHECK_TASK_DATA = InstructionType.ACTION_CHECK_TASK_DATA.getName();
    private static final String STR_ACTION_REPLACE_PHRASE_IN_SCRIPT = InstructionType.ACTION_REPLACE_PHRASE_IN_SCRIPT.getName();
    private static final String STR_ACTION_START_TASK = InstructionType.ACTION_START_TASK.getName();
    private static final String STR_ACTION_RESTART_TASK = InstructionType.ACTION_RESTART_TASK.getName();
    private static final String STR_ACTION_STOP_TASK = InstructionType.ACTION_STOP_TASK.getName();
    private static final String STR_ACTION_RECALL_TASK = InstructionType.ACTION_RECALL_TASK.getName();
    private static final String STR_ACTION_RESUME_TASK = InstructionType.ACTION_RESUME_TASK.getName();
    private static final String STR_ACTION_CHANGE_TENANT_LINE = InstructionType.ACTION_CHANGE_TENANT_LINE.getName();
    private static final String STR_ACTION_CHANGE_CONCURRENCY = InstructionType.ACTION_CHANGE_CONCURRENCY.getName();
    private static final String STR_ACTION_ADD_TASK = InstructionType.ACTION_ADD_TASK.getName();
    private static final String STR_ACTION_SET_LINE_RATIO = InstructionType.ACTION_SET_LINE_RATIO.getName();
    private static final String STR_ACTION_FORBID_DISTRICT = InstructionType.ACTION_FORBID_DISTRICT.getName();
    private static final String STR_ACTION_ALLOW_DISTRICT = InstructionType.ACTION_ALLOW_DISTRICT.getName();
    private static final String STR_ACTION_EDIT_SUPPLY_LINE = InstructionType.ACTION_EDIT_SUPPLY_LINE.getName();
    private static final String STR_ACTION_EDIT_TENANT_LINE = InstructionType.ACTION_EDIT_TENANT_LINE.getName();
    private static final String STR_ACTION_CREATE_MAIN_ACCOUNT = InstructionType.ACTION_CREATE_MAIN_ACCOUNT.getName();
    private static final String STR_ACTION_CREATE_SUB_ACCOUNT = InstructionType.ACTION_CREATE_SUB_ACCOUNT.getName();
    private static final String STR_ACTION_CREATE_ROLE = InstructionType.ACTION_CREATE_ROLE.getName();
    private static final String STR_ACTION_ADD_ROLE_IP = InstructionType.ACTION_ADD_ROLE_IP.getName();
    private static final String STR_ACTION_SET_ACCOUNT_OPERATOR_PARAM = InstructionType.ACTION_SET_ACCOUNT_OPERATOR_PARAM.getName();
    private static final String STR_ACTION_DOWNLOAD_AUDIO = InstructionType.ACTION_DOWNLOAD_AUDIO.getName();
    private static final String STR_ACTION_REPORT_CALLED_TASK_TEMPLATE = InstructionType.ACTION_REPORT_CALLED_TASK_TEMPLATE.getName();
    private static final String STR_ACTION_REPORT_ACCOUNT_LOCKED_CONCURRENCY = InstructionType.ACTION_REPORT_ACCOUNT_LOCKED_CONCURRENCY.getName();
    private static final String STR_ACTION_GET_TASK_TEMPLATE = InstructionType.ACTION_GET_TASK_TEMPLATE.getName();
    private static final String STR_ACTION_CREATE_TASK_TEMPLATE = InstructionType.ACTION_CREATE_TASK_TEMPLATE.getName();
    private static final String STR_ACTION_STOP_SCRIPT = InstructionType.ACTION_STOP_SCRIPT.getName();
    private static final String STR_ACTION_REPORT_SCRIPT_FOR_LINE = InstructionType.ACTION_REPORT_SCRIPT_FOR_LINE.getName();
    private static final String STR_ACTION_REPORT_SCRIPT_FOR_JIAFANG = InstructionType.ACTION_REPORT_SCRIPT_FOR_JIAFANG.getName();
    private static final String STR_ACTION_REPORT_TASK_STATISTIC = InstructionType.ACTION_REPORT_TASK_STATISTIC.getName();
    private static final String STR_ACTION_REPORT_TASK_SCRIPT_STATISTIC = InstructionType.ACTION_REPORT_TASK_SCRIPT_STATISTIC.getName();
    private static final String STR_ACTION_REPORT_RECENTLY_UPDATED_SCRIPT_STATISTIC = InstructionType.ACTION_REPORT_RECENTLY_UPDATED_SCRIPT_STATISTIC.getName();
    private static final String STR_ACTION_GET_PHONE_RECORD = InstructionType.ACTION_GET_PHONE_RECORD.getName();


    private static final Set<String> SPECIAL_CALL_ACTION_SET = Sets.newHashSet(
            STR_ACTION_RESTART_TASK, STR_ACTION_STOP_TASK, STR_ACTION_RECALL_TASK, STR_ACTION_RESUME_TASK,
            STR_ACTION_CHANGE_CONCURRENCY, STR_ACTION_CHANGE_TENANT_LINE, STR_ACTION_ADD_TASK
    );

    private static final Set<String> SINGLE_ACTION_SET = Sets.newHashSet(STR_ACTION_CREATE_TASK_TEMPLATE);

    private static List<AbstractInstructionBean> getInstructionBeanList(
            ChatGroup chatGroup, String creator, QueryInfo queryInfo, List<IntentInfo> intentInfoList, List<Slot> requiredSlotList) {
        List<AbstractInstructionBean> allInstructionBeanList = new LinkedList<>();
//        List<Slot> slotList = new ArrayList<>(requiredSlotList);
//        List<Slot> slotList = postprocess(requiredSlotList);
        List<Slot> allSlotList = postprocess(requiredSlotList);
        Map<String, List<Slot>> source2slotList = allSlotList.stream().collect(
                Collectors.groupingBy(x -> x.getEvidence(), LinkedHashMap::new, Collectors.toList()));
        List<String> intentList = intentInfoList.stream()
                .map(intentInfo -> intentInfo.getIntent()).collect(Collectors.toList());
        String instructionId = allSlotList.stream()
                .filter(slot -> STR_INSTRUCTION_ID.equals(slot.getSlotName()))
                .map(slot -> slot.getNormedValue())
                .findFirst().orElse(null);
        List<List<Slot>> slotGroupList = getSlotGroupList(allSlotList, queryInfo);
        // 如果有其他外呼操作，则删除”开始任务“操作
        boolean hasSpecialCall = intentList.stream().filter(intent -> SPECIAL_CALL_ACTION_SET.contains(intent)).count() > 0;
        if (hasSpecialCall) {
            for (int i = 0; i < intentList.size(); i++) {
                String intent = intentList.get(i);
                if (STR_ACTION_START_TASK.equals(intent)) {
                    intentList.remove(i);
                    i--;
                }
            }
        }
        for (String singleAction: SINGLE_ACTION_SET) {
            if (intentList.contains(singleAction)) {
                intentList = Lists.newArrayList(singleAction);
            }
        }
        if (intentList.contains(STR_ACTION_EDIT_SUPPLY_LINE) || intentList.contains(STR_ACTION_EDIT_TENANT_LINE)) {
            intentList.remove(STR_ACTION_CHANGE_CONCURRENCY);
            intentList.remove(STR_ACTION_CHANGE_TENANT_LINE);
        }

        if (intentList.contains(STR_ACTION_EXECUTE_INSTRUCTION)
                || intentList.contains(STR_ACTION_REMOVE_INSTRUCTION)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            String instructionIdToExecute = slotList.stream()
                    .filter(slot -> STR_EXECUTE_INSTRUCTION_ID.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            InstructionType instructionType;
            if (intentList.contains(STR_ACTION_REMOVE_INSTRUCTION)) {
                instructionType = InstructionType.ACTION_REMOVE_INSTRUCTION;
            } else {
                instructionType = InstructionType.ACTION_EXECUTE_INSTRUCTION;
            }
            ExecuteInstructionBean instructionBean = new ExecuteInstructionBean(null, instructionType, chatGroup, creator, instructionIdToExecute);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_INSTRUCTION)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            String instructionIdToExecute = slotList.stream()
                    .filter(slot -> STR_EXECUTE_INSTRUCTION_ID.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            ReportInstructionInstructionBean instructionBean = new ReportInstructionInstructionBean(instructionId, chatGroup, creator, instructionIdToExecute);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_FINISHED_INSTRUCTION)) {
            ReportFinishedInstructionInstructionBean instructionBean = new ReportFinishedInstructionInstructionBean(instructionId, chatGroup, creator);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_FORBID_DISTRICT_ALL_DAY)) {
            ReportForbiddenDistrictAllDay instructionBean = new ReportForbiddenDistrictAllDay(instructionId, chatGroup, creator);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_CALLED_TASK_TEMPLATE)) {
            ReportCalledTaskTemplateInstructionBean instructionBean = new ReportCalledTaskTemplateInstructionBean(instructionId, chatGroup, creator);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_ACCOUNT_LOCKED_CONCURRENCY)) {
            ReportAccountLockedConcurrencyInstructionBean instructionBean = new ReportAccountLockedConcurrencyInstructionBean(instructionId, chatGroup, creator);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_RECENTLY_UPDATED_SCRIPT_STATISTIC)) {
            ReportRecentlyUpdatedScriptStatisticInstructionBean instructionBean = new ReportRecentlyUpdatedScriptStatisticInstructionBean(instructionId, chatGroup, creator);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_GET_TASK_TEMPLATE)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            String templateId = slotList.stream()
                    .filter(slot -> STR_TEMPLATE_ID.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            GetTaskTemplateInstructionBean instructionBean = new GetTaskTemplateInstructionBean(instructionId, chatGroup, creator, templateId);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_CREATE_TASK_TEMPLATE)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            List<Pair<String, String>> workStartEndTimePairList = new ArrayList<>();
            for (Map.Entry<String, List<Slot>> entry: source2slotList.entrySet()) {
                String source = entry.getKey();
                List<Slot> tmpSlotList = entry.getValue();
                if (tmpSlotList.size() == 2
                        && isContainSlot(tmpSlotList, STR_EXPECTED_START_TIME)
                        && isContainSlot(tmpSlotList, STR_EXPECTED_END_TIME)) {
                    String expectedStartTime = tmpSlotList.stream()
                            .filter(slot -> STR_EXPECTED_START_TIME.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().get();
                    String expectedEndTime = tmpSlotList.stream()
                            .filter(slot -> STR_EXPECTED_END_TIME.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().get();
                    workStartEndTimePairList.add(new Pair<>(expectedStartTime, expectedEndTime));
                }
            }

            String account = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String templateName = slotList.stream()
                    .filter(slot -> STR_TEMPLATE_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            TaskType taskType = slotList.stream()
                    .filter(slot -> STR_TASK_TYPE.equals(slot.getSlotName()))
                    .map(slot -> TaskType.fromCaption(slot.getNormedValue()))
                    .findFirst().orElse(null);
            Boolean nextDayCall = slotList.stream()
                    .filter(slot -> STR_IS_NEXT_DAY_CALL.equals(slot.getSlotName()))
                    .map(slot -> STR_YES_SET.contains(slot.getNormedValue()))
                    .findFirst().orElse(null);
            String scriptName = slotList.stream()
                    .filter(slot -> STR_SCRIPT_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            List<String> callTeamNameList = slotList.stream()
                    .filter(slot -> STR_CALL_TEAM_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            HandleType handleType = slotList.stream()
                    .filter(slot -> STR_HANDLE_TYPE.equals(slot.getSlotName()))
                    .map(slot -> HandleType.fromCaption(slot.getNormedValue()))
                    .findFirst().orElse(null);
            String lineRatio = slotList.stream()
                    .filter(slot -> STR_LINE_RATIO.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            OccupyRate occupyRate = slotList.stream()
                    .filter(slot -> STR_OCCUPY_RATE.equals(slot.getSlotName()))
                    .map(slot -> OccupyRate.fromCaption(slot.getNormedValue()))
                    .findFirst().orElse(null);
            String virtualSeatRatio = slotList.stream()
                    .filter(slot -> STR_VIRTUAL_SEAT_RATIO.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            CreateTaskTemplateInstructionBean instructionBean = new CreateTaskTemplateInstructionBean(instructionId,
                    chatGroup, creator, account, templateName, taskType, nextDayCall, scriptName, callTeamNameList,
                    handleType, lineRatio, occupyRate, virtualSeatRatio, workStartEndTimePairList);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_STOP_SCRIPT)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            List<String> scriptNameList = slotList.stream()
                    .filter(slot -> STR_SCRIPT_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            StopScriptInstructionBean instructionBean = new StopScriptInstructionBean(instructionId, chatGroup, creator, scriptNameList);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_SCRIPT_FOR_LINE)
                || intentList.contains(STR_ACTION_REPORT_SCRIPT_FOR_JIAFANG)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            List<String> scriptNameList = slotList.stream()
                    .filter(slot -> STR_SCRIPT_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            InstructionType instructionType;
            if (intentList.contains(STR_ACTION_REPORT_SCRIPT_FOR_LINE)) {
                instructionType = InstructionType.ACTION_REPORT_SCRIPT_FOR_LINE;
            } else {
                instructionType = InstructionType.ACTION_REPORT_SCRIPT_FOR_JIAFANG;
            }
            ReportScriptForLineInstructionBean instructionBean = new ReportScriptForLineInstructionBean(instructionId, instructionType, chatGroup, creator, scriptNameList);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_TASK_STATISTIC)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            String strDate = null;
            for (Map.Entry<String, List<Slot>> entry: source2slotList.entrySet()) {
                String source = entry.getKey();
                List<Slot> tmpSlotList = entry.getValue();
                if (tmpSlotList.size() == 3
                        && isContainSlot(tmpSlotList, STR_YEAR)
                        && isContainSlot(tmpSlotList, STR_MONTH)
                        && isContainSlot(tmpSlotList, STR_DAY)) {
                    String year = slotList.stream()
                            .filter(slot -> STR_YEAR.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().get();
                    String month = slotList.stream()
                            .filter(slot -> STR_MONTH.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().get();
                    String day = slotList.stream()
                            .filter(slot -> STR_DAY.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().get();
                    strDate = year + "-" + month + "-" + day;
                }
            }
            if (strDate == null) {
                strDate = slotList.stream()
                        .filter(slot -> STR_DATE.equals(slot.getSlotName()))
                        .map(slot -> slot.getNormedValue())
                        .findFirst().orElse(null);
            }
            String account = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            List<String> intentionClassList = slotList.stream()
                    .filter(slot -> STR_INTENTION_CLASS.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            Boolean outputAvgCallDuration = slotList.stream()
                    .filter(slot -> STR_IS_OUTPUT_AVG_CALL_DURATION.equals(slot.getSlotName()))
                    .map(slot -> STR_YES_SET.contains(slot.getNormedValue()))
                    .findFirst().orElse(null);
            Boolean outputTotalCallDuration = slotList.stream()
                    .filter(slot -> STR_IS_OUTPUT_TOTAL_CALL_DURATION.equals(slot.getSlotName()))
                    .map(slot -> STR_YES_SET.contains(slot.getNormedValue()))
                    .findFirst().orElse(null);
            ReportTaskStatisticInstructionBean instructionBean = new ReportTaskStatisticInstructionBean(instructionId, chatGroup, creator, account, strDate, intentionClassList, outputAvgCallDuration, outputTotalCallDuration);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_REPORT_TASK_SCRIPT_STATISTIC)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            String strDate = null;
            for (Map.Entry<String, List<Slot>> entry: source2slotList.entrySet()) {
                String source = entry.getKey();
                List<Slot> tmpSlotList = entry.getValue();
                if (tmpSlotList.size() == 3
                        && isContainSlot(tmpSlotList, STR_YEAR)
                        && isContainSlot(tmpSlotList, STR_MONTH)
                        && isContainSlot(tmpSlotList, STR_DAY)) {
                    String year = slotList.stream()
                            .filter(slot -> STR_YEAR.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().get();
                    String month = slotList.stream()
                            .filter(slot -> STR_MONTH.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().get();
                    String day = slotList.stream()
                            .filter(slot -> STR_DAY.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().get();
                    strDate = year + "-" + month + "-" + day;
                }
            }
            if (strDate == null) {
                strDate = slotList.stream()
                        .filter(slot -> STR_DATE.equals(slot.getSlotName()))
                        .map(slot -> slot.getNormedValue())
                        .findFirst().orElse(null);
            }
            String account = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            List<String> taskNameList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            ReportTaskScriptStatisticInstructionBean instructionBean = new ReportTaskScriptStatisticInstructionBean(instructionId, chatGroup, creator, account, strDate, taskNameList);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_GET_PHONE_RECORD)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            String strStartDate = slotList.stream()
                    .filter(slot -> STR_START_DATE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String strEndDate = slotList.stream()
                    .filter(slot -> STR_END_DATE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String contentContain = slotList.stream()
                    .filter(slot -> STR_CONTENT_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Set<String> intentSet = slotList.stream()
                    .filter(slot -> STR_INTENT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toSet());
            String lineCodeContain = slotList.stream()
                    .filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())
                            || STR_FILTERED_TENANT_LINE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            if (strStartDate == null && strEndDate == null) {
                strStartDate = strEndDate = slotList.stream()
                        .filter(slot -> STR_DATE.equals(slot.getSlotName()))
                        .map(slot -> slot.getNormedValue())
                        .findFirst().orElse(null);
            }
            GetPhoneRecordInstructionBean instructionBean = new GetPhoneRecordInstructionBean(instructionId, chatGroup, creator, strStartDate, strEndDate, contentContain, intentSet, lineCodeContain);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_CHECK_TASK_DATA)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            List<Pair<String, String>> productModePairList = new ArrayList<>();
            List<Pair<String, String>> productAccountSuffixPairList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() == 2) {
                    if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ENTRANCE_MODE)) {
                        // slot模板同时抽取【产品名称】和【入口模式】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot entranceModeSlot = tmpSlotList.stream().filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productModePair = new Pair<>(productSlot.getNormedValue(), entranceModeSlot.getNormedValue());
                        productModePairList.add(productModePair);
                        slotList.remove(productSlot);
                        slotList.remove(entranceModeSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ACCOUNT_SUFFIX)) {
                        // slot模板同时抽取【产品名称】和【账号后缀】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot accountSuffixSlot = tmpSlotList.stream().filter(slot -> STR_ACCOUNT_SUFFIX.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productAccountSufixPair = new Pair<>(productSlot.getNormedValue(), accountSuffixSlot.getNormedValue());
                        productAccountSuffixPairList.add(productAccountSufixPair);
                        slotList.remove(productSlot);
                        slotList.remove(accountSuffixSlot);
                    }
                }
            });
            slotList.stream()
                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .distinct()
                    .forEach(product -> productModePairList.add(new Pair<>(product, null)));
            slotList.stream()
                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .distinct()
                    .forEach(entranceMode -> productModePairList.add(new Pair<>(null, entranceMode)));
            if (productModePairList.isEmpty()) {
                productModePairList.add(new Pair<>(null, null));
            }
            Set<String> accountSet = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(HashSet::new));
            List<AccountProductInfo> accountProductInfoList = getAccountProductInfoList(accountSet, productAccountSuffixPairList, productModePairList);
            for (AccountProductInfo info: accountProductInfoList) {
                String account = info.getAccount();
                String product = info.getProduct();
                String entranceMode = info.getEntranceMode();
//                InstructionType instructionType = InstructionType.ACTION_CHECK_TASK_DATA;
                CheckDataInstructionBean instructionBean = new CheckDataInstructionBean(
                        instructionId, chatGroup, creator, account, product, entranceMode);
                allInstructionBeanList.add(instructionBean);
            }
        } else if (intentList.contains(STR_ACTION_REPLACE_PHRASE_IN_SCRIPT)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            ReplaceType replaceType = slotList.stream()
                    .filter(slot -> STR_REPLACE_TYPE.equals(slot.getSlotName()))
                    .map(slot -> ReplaceType.fromCaption(slot.getNormedValue()))
                    .findFirst().orElse(null);
            String scriptName = slotList.stream()
                    .filter(slot -> STR_SCRIPT_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String oldPhrase = slotList.stream()
                    .filter(slot -> STR_OLD_PHRASE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String newPhrase = slotList.stream()
                    .filter(slot -> STR_NEW_PHRASE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            ReplacePhraseInScriptInstructionBean instructionBean = new ReplacePhraseInScriptInstructionBean(
                    instructionId, chatGroup, creator, replaceType, scriptName, oldPhrase, newPhrase);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_START_TASK)
                || intentList.contains(STR_ACTION_RESTART_TASK)
                || intentList.contains(STR_ACTION_RECALL_TASK)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            List<TaskInfoBean> taskInfoBeanList = new ArrayList<>();
            List<Pair<String, String>> productModePairList = new ArrayList<>();
            List<Pair<String, String>> productAccountSuffixPairList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() > 2
                        && isOnlyContainSlots(tmpSlotList, Sets.newHashSet(STR_TENANT_LINE, STR_CALLING_NUMBER, STR_CONCURRENCY))
                        && getSlotCount(tmpSlotList, STR_TENANT_LINE) == getSlotCount(tmpSlotList, STR_CALLING_NUMBER)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【线路方】+、【主叫号码】+、【并发数】的
                    List<Slot> lineSlotList = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<Slot> callingNumberSlotList = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).collect(Collectors.toList());
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                    String expectedStartTime = null;
                    String expectedEndTime = null;
                    Slot firstLineSlot = lineSlotList.get(0);
                    Slot expectedStartTimeSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    Slot expectedEndTimeSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_EXPECTED_END_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    if (expectedStartTimeSlot != null) {
                        expectedStartTime = expectedStartTimeSlot.getNormedValue();
                        slotList.remove(expectedStartTimeSlot);
                    }
                    if (expectedEndTimeSlot != null) {
                        expectedEndTime = expectedEndTimeSlot.getNormedValue();
                        slotList.remove(expectedEndTimeSlot);
                    }
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualLisy = Collections.emptyList();
                    List<Slot> taskNameEqualSlotList = slotList.stream()
                            .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                            .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                    Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);

                    String concurrency = concurrencySlot.getNormedValue();
                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }
                    String expectedConnectedCallCount = null;
                    if (expectedConnectedCallCountSlot != null) {
                        expectedConnectedCallCount = expectedConnectedCallCountSlot.getNormedValue();
                    }
                    for (int i = 0; i < lineSlotList.size(); i++) {
                        String tenantLine = lineSlotList.get(i).getNormedValue();
                        String callingNumber = callingNumberSlotList.get(i).getNormedValue();
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                    }
                    slotList.removeAll(lineSlotList);
                    slotList.removeAll(callingNumberSlotList);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameEqualSlotList);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                    slotList.remove(expectedConnectedCallCountSlot);
                } else if (tmpSlotList.size() > 2
                        && isContainSlot(tmpSlotList, STR_TENANT_LINE, 1)
                        && isContainSlot(tmpSlotList, STR_CALLING_NUMBER, tmpSlotList.size() - 2)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【线路方】、【主叫号码】+、【并发数】的
                    Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                    List<Slot> callingNumberSlotList = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).collect(Collectors.toList());
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                    String expectedStartTime = null;
                    String expectedEndTime = null;
                    Slot expectedStartTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    Slot expectedEndTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_END_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    if (expectedStartTimeSlot != null) {
                        expectedStartTime = expectedStartTimeSlot.getNormedValue();
                        slotList.remove(expectedStartTimeSlot);
                    }
                    if (expectedEndTimeSlot != null) {
                        expectedEndTime = expectedEndTimeSlot.getNormedValue();
                        slotList.remove(expectedEndTimeSlot);
                    }
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualLisy = Collections.emptyList();
                    List<Slot> taskNameEqualSlotList = slotList.stream()
                            .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                            .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                    Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);

                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }

                    String tenantLine = lineSlot.getNormedValue();
                    String concurrency = concurrencySlot.getNormedValue();
                    String expectedConnectedCallCount = null;
                    if (expectedConnectedCallCountSlot != null) {
                        expectedConnectedCallCount = expectedConnectedCallCountSlot.getNormedValue();
                    }
                    for (Slot callingNumberSlot: callingNumberSlotList) {
                        String callingNumber = callingNumberSlot.getNormedValue();
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd,tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                    }
                    slotList.remove(lineSlot);
                    slotList.removeAll(callingNumberSlotList);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameEqualSlotList);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                    slotList.remove(expectedConnectedCallCountSlot);
                } else if (tmpSlotList.size() > 2
                        && isContainSlot(tmpSlotList, STR_TENANT_LINE, tmpSlotList.size() - 2)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【线路方】+、【并发数】的
                    List<Slot> lineSlotList = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).collect(Collectors.toList());
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                    String expectedStartTime = null;
                    String expectedEndTime = null;
                    Slot firstLineSlot = lineSlotList.get(0);
                    Slot expectedStartTimeSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    Slot expectedEndTimeSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_EXPECTED_END_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    if (expectedStartTimeSlot != null) {
                        expectedStartTime = expectedStartTimeSlot.getNormedValue();
                        slotList.remove(expectedStartTimeSlot);
                    }
                    if (expectedEndTimeSlot != null) {
                        expectedEndTime = expectedEndTimeSlot.getNormedValue();
                        slotList.remove(expectedEndTimeSlot);
                    }
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualLisy = Collections.emptyList();
                    List<Slot> taskNameEqualSlotList = slotList.stream()
                            .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                            .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                    Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(firstLineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);

                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }

                    String concurrency = concurrencySlot.getNormedValue();
                    String expectedConnectedCallCount = null;
                    if (expectedConnectedCallCountSlot != null) {
                        expectedConnectedCallCount = expectedConnectedCallCountSlot.getNormedValue();
                    }
                    for (Slot lineSlot: lineSlotList) {
                        String tenantLine = lineSlot.getNormedValue();
                        String callingNumber = null;
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                    }
                    slotList.removeAll(lineSlotList);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameEqualSlotList);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                    slotList.remove(expectedConnectedCallCountSlot);
                } else if (tmpSlotList.size() > 2
                        && isContainSlot(tmpSlotList, STR_TASK_NAME_CONTAIN, tmpSlotList.size() - 2)
                        && isContainSlot(tmpSlotList, STR_TENANT_LINE, 1)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【任务名称包含】+、【线路方】、【并发数】的
                    Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                    Slot callingNumberSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_CALLING_NUMBER, slotGroupList).stream().findFirst().orElse(null);
                    String callingNumber = null;
                    if (callingNumberSlot != null) {
                        callingNumber = callingNumberSlot.getNormedValue();
                    }
                    String expectedStartTime = null;
                    String expectedEndTime = null;
                    Slot expectedStartTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    Slot expectedEndTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_END_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    if (expectedStartTimeSlot != null) {
                        expectedStartTime = expectedStartTimeSlot.getNormedValue();
                        slotList.remove(expectedStartTimeSlot);
                    }
                    if (expectedEndTimeSlot != null) {
                        expectedEndTime = expectedEndTimeSlot.getNormedValue();
                        slotList.remove(expectedEndTimeSlot);
                    }
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
                    List<String> taskNameEqualList = Collections.emptyList();
//                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<Slot> taskNameContainSlotList = tmpSlotList.stream().filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                    Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);

                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }

                    String tenantLine = lineSlot.getNormedValue();
                    String concurrency = concurrencySlot.getNormedValue();
                    String expectedConnectedCallCount = null;
                    if (expectedConnectedCallCountSlot != null) {
                        expectedConnectedCallCount = expectedConnectedCallCountSlot.getNormedValue();
                    }
                    TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                    taskInfoBeanList.add(taskInfoBean);
                    slotList.remove(lineSlot);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                    slotList.remove(taskCreateTimeBoundStartSlot);
                    slotList.remove(taskCreateTimeBoundEndSlot);
                    slotList.remove(expectedConnectedCallCountSlot);
                } else if (isOnlyContainSlots(tmpSlotList, Sets.newHashSet(STR_TASK_NAME_CONTAIN, STR_TENANT_LINE, STR_CALLING_NUMBER,  STR_CONCURRENCY))
                        && isContainSlot(tmpSlotList, STR_TENANT_LINE, 1)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【任务名称包含】+、【线路方】、【主叫号码】+、【并发数】的
                    Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                    List<Slot> callingNumberSlotList = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).collect(Collectors.toList());
                    String expectedStartTime = null;
                    String expectedEndTime = null;
                    Slot expectedStartTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    Slot expectedEndTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_END_TIME, slotGroupList)
                            .stream().findFirst().orElse(null);
                    if (expectedStartTimeSlot != null) {
                        expectedStartTime = expectedStartTimeSlot.getNormedValue();
                        slotList.remove(expectedStartTimeSlot);
                    }
                    if (expectedEndTimeSlot != null) {
                        expectedEndTime = expectedEndTimeSlot.getNormedValue();
                        slotList.remove(expectedEndTimeSlot);
                    }
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualList = Collections.emptyList();
                    List<Slot> taskNameEqualSlotList = slotList.stream()
                            .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                            .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
//                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<Slot> taskNameContainSlotList = tmpSlotList.stream().filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                    Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);

                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }

                    String tenantLine = lineSlot.getNormedValue();
                    String concurrency = concurrencySlot.getNormedValue();
                    String expectedConnectedCallCount = null;
                    if (expectedConnectedCallCountSlot != null) {
                        expectedConnectedCallCount = expectedConnectedCallCountSlot.getNormedValue();
                    }
                    for (Slot callingNumberSlot: callingNumberSlotList) {
                        String callingNumber = callingNumberSlot.getNormedValue();
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                    }
                    slotList.remove(lineSlot);
                    slotList.removeAll(callingNumberSlotList);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameEqualSlotList);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                    slotList.remove(expectedConnectedCallCountSlot);
                } else if (tmpSlotList.size() == 3) {
                    // slot模板同时抽取【预期开始时间】、【预期完成时间】、【线路方】的
                    if (isContainSlot(tmpSlotList, STR_TENANT_LINE)
                            && isContainSlot(tmpSlotList, STR_EXPECTED_START_TIME)
                            && isContainSlot(tmpSlotList, STR_EXPECTED_END_TIME)) {
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot expectedStartTimeSlot = tmpSlotList.stream().filter(slot -> STR_EXPECTED_START_TIME.equals(slot.getSlotName())).findFirst().get();
                        Slot expectedEndTimeSlot = tmpSlotList.stream().filter(slot -> STR_EXPECTED_END_TIME.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_CALLING_NUMBER, slotGroupList).stream().findFirst().orElse(null);

//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualList = Collections.emptyList();
                        List<Slot> taskNameEqualSlotList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                        List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                                .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                        List<String> taskNameContainList = taskNameContainSlotList
                                .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                        List<String> taskNameNotContainList = taskNameNotContainSlotList
                                .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                        List<String> taskNameSuffixList = taskNameSuffixSlotList
                                .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                        Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                        Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                        Slot concurrencySlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList).stream().findFirst().orElse(null);
                        Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);

                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        if (taskCreateTimeBoundPeriodSlot != null) {
                            Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                            if (pair != null) {
                                taskCreateTimeBoundStart = pair.getKey();
                                taskCreateTimeBoundEnd = pair.getValue();
                                slotList.remove(taskCreateTimeBoundPeriodSlot);
                            }
                        }
                        if (taskCreateTimeBoundStartSlot != null) {
                            taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                            slotList.remove(taskCreateTimeBoundStartSlot);
                        }
                        if (taskCreateTimeBoundEndSlot != null) {
                            taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                            slotList.remove(taskCreateTimeBoundEndSlot);
                        }

                        String tenantLine = lineSlot.getNormedValue();
                        String expectedStartTime = expectedStartTimeSlot.getNormedValue();
                        String expectedEndTime = expectedEndTimeSlot.getNormedValue();
                        String callingNumber = null;
                        if (callingNumberSlot != null) {
                            callingNumber = callingNumberSlot.getNormedValue();
                            slotList.remove(callingNumberSlot);
                        }
                        String concurrency = null;
                        if (concurrencySlot != null) {
                            concurrency = concurrencySlot.getNormedValue();
                            slotList.remove(concurrencySlot);
                        }
                        String expectedConnectedCallCount = null;
                        if (expectedConnectedCallCountSlot != null) {
                            expectedConnectedCallCount = expectedConnectedCallCountSlot.getNormedValue();
                        }
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                        slotList.remove(lineSlot);
                        slotList.remove(expectedStartTimeSlot);
                        slotList.remove(expectedEndTimeSlot);
                        slotList.removeAll(taskNameEqualSlotList);
                        slotList.removeAll(taskNameContainSlotList);
                        slotList.removeAll(taskNameNotContainSlotList);
                        slotList.removeAll(taskNameSuffixSlotList);
                        slotList.remove(expectedConnectedCallCountSlot);
                    }
                } else if (tmpSlotList.size() == 2) {
                    if (isContainSlot(tmpSlotList, STR_TENANT_LINE) && isContainSlot(tmpSlotList, STR_CONCURRENCY)) {
                        // slot模板同时抽取【线路方】和【并发数】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_CALLING_NUMBER, slotGroupList).stream().findFirst().orElse(null);
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualLisy = Collections.emptyList();
                        List<Slot> taskNameEqualSlotList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                        List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                                .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                        List<String> taskNameContainList = taskNameContainSlotList
                                .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                        List<String> taskNameNotContainList = taskNameNotContainSlotList
                                .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                        List<String> taskNameSuffixList = taskNameSuffixSlotList
                                .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                        Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                        Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);

                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        if (taskCreateTimeBoundPeriodSlot != null) {
                            Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                            if (pair != null) {
                                taskCreateTimeBoundStart = pair.getKey();
                                taskCreateTimeBoundEnd = pair.getValue();
                                slotList.remove(taskCreateTimeBoundPeriodSlot);
                            }
                        }
                        if (taskCreateTimeBoundStartSlot != null) {
                            taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                            slotList.remove(taskCreateTimeBoundStartSlot);
                        }
                        if (taskCreateTimeBoundEndSlot != null) {
                            taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                            slotList.remove(taskCreateTimeBoundEndSlot);
                        }

                        String tenantLine = lineSlot.getNormedValue();
                        String concurrency = concurrencySlot.getNormedValue();
                        String callingNumber = null;
                        if (callingNumberSlot != null) {
                            callingNumber = callingNumberSlot.getNormedValue();
                            slotList.remove(callingNumberSlot);
                        }
                        String expectedStartTime = null;
                        String expectedEndTime = null;
                        Slot expectedStartTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                                .stream().findFirst().orElse(null);
                        Slot expectedEndTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_END_TIME, slotGroupList)
                                .stream().findFirst().orElse(null);
                        if (expectedStartTimeSlot != null) {
                            expectedStartTime = expectedStartTimeSlot.getNormedValue();
                            slotList.remove(expectedStartTimeSlot);
                        }
                        if (expectedEndTimeSlot != null) {
                            expectedEndTime = expectedEndTimeSlot.getNormedValue();
                            slotList.remove(expectedEndTimeSlot);
                        }
                        Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);
                        String expectedConnectedCallCount = null;
                        if (expectedConnectedCallCountSlot != null) {
                            expectedConnectedCallCount = expectedConnectedCallCountSlot.getNormedValue();
                        }
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                        slotList.remove(lineSlot);
                        slotList.remove(concurrencySlot);
                        slotList.removeAll(taskNameEqualSlotList);
                        slotList.removeAll(taskNameContainSlotList);
                        slotList.removeAll(taskNameNotContainSlotList);
                        slotList.removeAll(taskNameSuffixSlotList);
                        slotList.remove(expectedConnectedCallCountSlot);
                    } else if (isContainSlot(tmpSlotList, STR_TENANT_LINE) && isContainSlot(tmpSlotList, STR_CALLING_NUMBER)) {
                        // slot模板同时抽取【线路方】和【主叫号码】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        String concurrency = null;
                        String expectedStartTime = null;
                        String expectedEndTime = null;
                        Slot concurrencySlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                                .stream().findFirst().orElse(null);
                        Slot expectedStartTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                                .stream().findFirst().orElse(null);
                        Slot expectedEndTimeSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_END_TIME, slotGroupList)
                                .stream().findFirst().orElse(null);
                        if (concurrencySlot != null) {
                            concurrency = concurrencySlot.getNormedValue();
                            slotList.remove(concurrencySlot);
                        }
                        if (expectedStartTimeSlot != null) {
                            expectedStartTime = expectedStartTimeSlot.getNormedValue();
                            slotList.remove(expectedStartTimeSlot);
                        }
                        if (expectedEndTimeSlot != null) {
                            expectedEndTime = expectedEndTimeSlot.getNormedValue();
                            slotList.remove(expectedEndTimeSlot);
                        }
//                        List<String> taskNameEqualList = Collections.emptyList();
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
                        List<Slot> taskNameEqualSlotList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                        List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                                .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                        List<String> taskNameContainList = taskNameContainSlotList.stream()
                                .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                        List<String> taskNameNotContainList = taskNameNotContainSlotList.stream()
                                .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                        List<String> taskNameSuffixList = taskNameSuffixSlotList
                                .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                        Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                        Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                        Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);

                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        if (taskCreateTimeBoundPeriodSlot != null) {
                            Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                            if (pair != null) {
                                taskCreateTimeBoundStart = pair.getKey();
                                taskCreateTimeBoundEnd = pair.getValue();
                                slotList.remove(taskCreateTimeBoundPeriodSlot);
                            }
                        }
                        if (taskCreateTimeBoundStartSlot != null) {
                            taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                            slotList.remove(taskCreateTimeBoundStartSlot);
                        }
                        if (taskCreateTimeBoundEndSlot != null) {
                            taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                            slotList.remove(taskCreateTimeBoundEndSlot);
                        }

                        String tenantLine = lineSlot.getNormedValue();
                        String expectedConnectedCallCount = null;
                        if (expectedConnectedCallCountSlot != null) {
                            expectedConnectedCallCount = expectedConnectedCallCountSlot.getNormedValue();
                        }
                        String callingNumber = callingNumberSlot.getNormedValue();
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                        slotList.remove(lineSlot);
                        slotList.remove(callingNumberSlot);
                        slotList.removeAll(taskNameEqualSlotList);
                        slotList.removeAll(taskNameContainSlotList);
                        slotList.removeAll(taskNameNotContainSlotList);
                        slotList.removeAll(taskNameSuffixSlotList);
                        slotList.remove(expectedConnectedCallCountSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ENTRANCE_MODE)) {
                        // slot模板同时抽取【产品名称】和【入口模式】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot entranceModeSlot = tmpSlotList.stream().filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productModePair = new Pair<>(productSlot.getNormedValue(), entranceModeSlot.getNormedValue());
                        productModePairList.add(productModePair);
                        slotList.remove(productSlot);
                        slotList.remove(entranceModeSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ACCOUNT_SUFFIX)) {
                        // slot模板同时抽取【产品名称】和【账号后缀】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot accountSuffixSlot = tmpSlotList.stream().filter(slot -> STR_ACCOUNT_SUFFIX.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productAccountSufixPair = new Pair<>(productSlot.getNormedValue(), accountSuffixSlot.getNormedValue());
                        productAccountSuffixPairList.add(productAccountSufixPair);
                        slotList.remove(productSlot);
                        slotList.remove(accountSuffixSlot);
                    }
                }
            });
            if (isContainSlot(slotList, STR_TENANT_LINE) || isContainSlot(slotList, STR_CALLING_NUMBER) || isContainSlot(slotList, STR_CONCURRENCY)) {
                if (isContainSlot(slotList, STR_TENANT_LINE)) {
                    List<Slot> tenantLineSlotList = slotList.stream()
                            .filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName()))
                            .collect(Collectors.toList());
                    for (Slot tenantLineSlot : tenantLineSlotList) {
                        String tenantLine = tenantLineSlot.getNormedValue();
                        String callingNumber = null;
                        String concurrency = null;
                        String expectedStartTime = null;
                        String expectedEndTime = null;
                        String expectedConnectedCallCount = null;
                        List<String> taskNameEqualList = Collections.emptyList();
                        List<String> taskNameContainList = Collections.emptyList();
                        List<String> taskNameNotContainList = Collections.emptyList();
                        List<String> taskNameSuffixList = Collections.emptyList();
                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        Slot callingNumberSlot = getExclusiveSlotsForLineSlot(tenantLineSlot, STR_CALLING_NUMBER, slotGroupList).stream().findFirst().orElse(null);
                        if (callingNumberSlot != null) {
                            callingNumber = callingNumberSlot.getNormedValue();
                        }
                        List<Slot> concurrencySlotList = slotList.stream()
                                .filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName()))
                                .collect(Collectors.toList());
                        if (concurrencySlotList.size() > 0) { // 只看第一个并发
                            concurrency = concurrencySlotList.get(0).getNormedValue();
                        }
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                    }
                } else if (isContainSlot(slotList, STR_CALLING_NUMBER)) {
                    List<Slot> callingNumberSlotList = slotList.stream()
                            .filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName()))
                            .collect(Collectors.toList());
                    for (Slot callingNumberSlot : callingNumberSlotList) {
                        String callingNumber = callingNumberSlot.getNormedValue();
                        String tenantLine = null;
                        String concurrency = null;
                        String expectedStartTime = null;
                        String expectedEndTime = null;
                        String expectedConnectedCallCount = null;
                        List<String> taskNameEqualList = Collections.emptyList();
                        List<String> taskNameContainList = Collections.emptyList();
                        List<String> taskNameNotContainList = Collections.emptyList();
                        List<String> taskNameSuffixList = Collections.emptyList();
                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        Slot tenantLineSlot = getExclusiveSlotsForLineSlot(callingNumberSlot, STR_TENANT_LINE, slotGroupList).stream().findFirst().orElse(null);
                        if (tenantLineSlot != null) {
                            tenantLine = tenantLineSlot.getNormedValue();
                        }
                        List<Slot> concurrencySlotList = slotList.stream()
                                .filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName()))
                                .collect(Collectors.toList());
                        if (concurrencySlotList.size() > 0) { // 只看第一个并发
                            concurrency = concurrencySlotList.get(0).getNormedValue();
                        }
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                    }
                } else if (isContainSlot(slotList, STR_CONCURRENCY)) {
                    // 只有并发数，没有线路方、主叫号码
                    if (taskInfoBeanList.size() == 1 && StringUtils.isEmpty(taskInfoBeanList.get(0).getConcurrency())) {
                        String concurrency = slotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue()).findFirst().get();
                        taskInfoBeanList.get(0).setConcurrency(concurrency);
                    } else {
                        String tenantLine = null;
                        String callingNumber = null;
//                    String concurrency = null;
                        String expectedStartTime = null;
                        String expectedEndTime = null;
                        String expectedConnectedCallCount = null;
                        List<String> taskNameEqualList = Collections.emptyList();
                        List<String> taskNameContainList = Collections.emptyList();
                        List<String> taskNameNotContainList = Collections.emptyList();
                        List<String> taskNameSuffixList = Collections.emptyList();
                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
//                    List<Slot> concurrencySlotList = slotList.stream()
//                            .filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName()))
//                            .collect(Collectors.toList());
//                    if (concurrencySlotList.size() > 0) { // 只看第一个并发
//                        concurrency = concurrencySlotList.get(0).getNormedValue();
//                    }
                        String concurrency = slotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue()).findFirst().get();
                        TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                        taskInfoBeanList.add(taskInfoBean);
                    }
                }
            } else if (taskInfoBeanList.isEmpty()) {  // 没有线路商，没有并发数，但有预期开始/完成时间
                if (isContainSlot(slotList, STR_EXPECTED_START_TIME)
                        || isContainSlot(slotList, STR_EXPECTED_END_TIME)
                        || isContainSlot(slotList, STR_EXPECTED_CONNECTED_CALL_COUNT)) {
                    String tenantLine = null;
                    String callingNumber = null;
                    String concurrency = null;
                    String expectedStartTime = slotList.stream()
                            .filter(slot -> STR_EXPECTED_START_TIME.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().orElse(null);
                    String expectedEndTime = slotList.stream()
                            .filter(slot -> STR_EXPECTED_END_TIME.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().orElse(null);
                    String expectedConnectedCallCount = slotList.stream()
                            .filter(slot -> STR_EXPECTED_CONNECTED_CALL_COUNT.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().orElse(null);
                    List<String> taskNameEqualList = Collections.emptyList();
                    List<String> taskNameContainList = Collections.emptyList();
                    List<String> taskNameNotContainList = Collections.emptyList();
                    List<String> taskNameSuffixList = Collections.emptyList();
                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                    taskInfoBeanList.add(taskInfoBean);
                }
            }
            List<String> taskNameEqualList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameNotContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameSuffixList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundStartSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundEndSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            String taskCreateTimeBoundStart = null;
            String taskCreateTimeBoundEnd = null;
            if (taskCreateTimeBoundPeriodSlot != null) {
                Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                if (pair != null) {
                    taskCreateTimeBoundStart = pair.getKey();
                    taskCreateTimeBoundEnd = pair.getValue();
//                    slotList.remove(taskCreateTimeBoundPeriodSlot);
                }
            }
            if (taskCreateTimeBoundStartSlot != null) {
                taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundStartSlot);
            }
            if (taskCreateTimeBoundEndSlot != null) {
                taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundEndSlot);
            }
            String expectedStartTime = slotList.stream()
                    .filter(slot -> STR_EXPECTED_START_TIME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String expectedEndTime = slotList.stream()
                    .filter(slot -> STR_EXPECTED_END_TIME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String expectedConnectedCallCount = slotList.stream()
                    .filter(slot -> STR_EXPECTED_CONNECTED_CALL_COUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            if (taskInfoBeanList.isEmpty()) {
                // 没有线路信息
                if (!CollectionUtils.isEmpty(taskNameEqualList)
                        || !CollectionUtils.isEmpty(taskNameContainList)
                        || !CollectionUtils.isEmpty(taskNameNotContainList)
                        || !CollectionUtils.isEmpty(taskNameSuffixList)
                        || !StringUtils.isEmpty(taskCreateTimeBoundStart)
                        || !StringUtils.isEmpty(taskCreateTimeBoundEnd)
                        || !StringUtils.isEmpty(expectedStartTime)
                        || !StringUtils.isEmpty(expectedEndTime)
                        || !StringUtils.isEmpty(expectedConnectedCallCount)
                ) {
                    TaskInfoBean taskInfoBean = new TaskInfoBean(taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, null, null, null,expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                    taskInfoBeanList.add(taskInfoBean);
                }
            } else {
                for (TaskInfoBean taskInfoBean : taskInfoBeanList) {
                    if (!CollectionUtils.isEmpty(taskNameEqualList) && CollectionUtils.isEmpty(taskInfoBean.getTaskNameEqualList())) {
                        taskInfoBean.setTaskNameEqualList(taskNameEqualList);
                    }
                    if (!CollectionUtils.isEmpty(taskNameContainList) && CollectionUtils.isEmpty(taskInfoBean.getTaskNameContainList())) {
                        taskInfoBean.setTaskNameContainList(taskNameContainList);
                    }
                    if (!CollectionUtils.isEmpty(taskNameNotContainList) && CollectionUtils.isEmpty(taskInfoBean.getTaskNameNotContainList())) {
                        taskInfoBean.setTaskNameNotContainList(taskNameNotContainList);
                    }
                    if (!CollectionUtils.isEmpty(taskNameSuffixList) && CollectionUtils.isEmpty(taskInfoBean.getTaskNameSuffixList())) {
                        taskInfoBean.setTaskNameSuffixList(taskNameSuffixList);
                    }
                    if (!StringUtils.isEmpty(taskCreateTimeBoundStart) && StringUtils.isEmpty(taskInfoBean.getTaskCreateTimeBoundStart())) {
                        taskInfoBean.setTaskCreateTimeBoundStart(taskCreateTimeBoundStart);
                    }
                    if (!StringUtils.isEmpty(taskCreateTimeBoundEnd) && StringUtils.isEmpty(taskInfoBean.getTaskCreateTimeBoundEnd())) {
                        taskInfoBean.setTaskCreateTimeBoundEnd(taskCreateTimeBoundEnd);
                    }
                    if (expectedStartTime != null && taskInfoBean.getExpectedStartTime() == null) {
                        taskInfoBean.setExpectedStartTime(expectedStartTime);
                    }
                    if (expectedEndTime != null && taskInfoBean.getExpectedEndTime() == null) {
                        taskInfoBean.setExpectedEndTime(expectedEndTime);
                    }
                    if (expectedConnectedCallCount != null && taskInfoBean.getExpectedConnectedCallCount() == null) {
                        taskInfoBean.setExpectedConnectedCallCount(expectedConnectedCallCount);
                    }
                }
            }
            slotList.stream()
                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .distinct()
                    .forEach(product -> productModePairList.add(new Pair<>(product, null)));
            slotList.stream()
                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .distinct()
                    .forEach(entranceMode -> productModePairList.add(new Pair<>(null, entranceMode)));
            if (productModePairList.isEmpty()) {
                productModePairList.add(new Pair<>(null, null));
            }

            TaskType taskType = slotList.stream()
                    .filter(slot -> STR_TASK_TYPE.equals(slot.getSlotName()))
                    .map(slot -> TaskType.fromCaption(slot.getNormedValue()))
                    .findFirst().orElse(null);
            if (taskType == null) {
                taskType = TaskType.AI_AUTO;
            }

            Set<TaskStatus> taskStatusSet = slotList.stream()
                    .filter(slot -> STR_TASK_STATUS.equals(slot.getSlotName()))
                    .map(slot -> TaskStatus.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            boolean useOriginalConcurrency = slotList.stream()
                    .anyMatch(slot -> STR_ORIGINAL_CONCURRENCY.equals(slot.getSlotName()));

            InstructionType instructionType;
            if (intentList.contains(STR_ACTION_RESTART_TASK)) {
                instructionType = InstructionType.ACTION_RESTART_TASK;
            } else if (intentList.contains(STR_ACTION_RECALL_TASK)) {
                instructionType = InstructionType.ACTION_RECALL_TASK;
            } else {
                instructionType = InstructionType.ACTION_START_TASK;
            }

            if (CollectionUtils.isEmpty(taskStatusSet)) {
                // 对于任务类型为空的，设置默认任务类型
                if (instructionType == InstructionType.ACTION_RESTART_TASK) {
                    taskStatusSet.add(TaskStatus.INCOMPLETE);
                } else if (instructionType == InstructionType.ACTION_RECALL_TASK) {
                    taskStatusSet.add(TaskStatus.INCOMPLETE);
                    taskStatusSet.add(TaskStatus.STOPPED);
                } else {
                    taskStatusSet.add(TaskStatus.UNEXCUTED);
                }
            }

            Set<String> accountSet = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(HashSet::new));
            List<AccountProductInfo> accountProductInfoList = getAccountProductInfoList(accountSet, productAccountSuffixPairList, productModePairList);
            for (AccountProductInfo info: accountProductInfoList) {
                String account = info.getAccount();
                String product = info.getProduct();
                String entranceMode = info.getEntranceMode();
                StartTaskInstructionBean instructionBean = new StartTaskInstructionBean(
                        instructionId, instructionType, chatGroup, creator, account, product, entranceMode, taskType, taskStatusSet, taskInfoBeanList, useOriginalConcurrency);
                allInstructionBeanList.add(instructionBean);
            }
        } else if (intentList.contains(STR_ACTION_STOP_TASK)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

//            List<String> accountList = null;
            List<Pair<String, String>> productModePairList = new ArrayList<>();
            List<Pair<String, String>> productAccountSuffixPairList = new ArrayList<>();
            List<Pair<String, String>> tenantLinePairList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() == 2) {
                    if (isContainSlot(tmpSlotList, STR_FILTERED_TENANT_LINE) && isContainSlot(tmpSlotList, STR_FILTERED_CALLING_NUMBER)) {
                        // slot模板同时抽取【筛选线路方】和【筛选主叫号码】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        String tenantLine = lineSlot.getNormedValue();
                        String callingNumber = callingNumberSlot.getNormedValue();

                        tenantLinePairList.add(new Pair<>(tenantLine, callingNumber));

                        slotList.remove(lineSlot);
                        slotList.remove(callingNumberSlot);
                    } else if (isContainSlot(tmpSlotList, STR_TENANT_LINE) && isContainSlot(tmpSlotList, STR_CALLING_NUMBER)) {
                        // slot模板同时抽取【线路方】和【主叫号码】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        String tenantLine = lineSlot.getNormedValue();
                        String callingNumber = callingNumberSlot.getNormedValue();

                        tenantLinePairList.add(new Pair<>(tenantLine, callingNumber));

                        slotList.remove(lineSlot);
                        slotList.remove(callingNumberSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ENTRANCE_MODE)) {
                        // slot模板同时抽取【产品名称】和【入口模式】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot entranceModeSlot = tmpSlotList.stream().filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productModePair = new Pair<>(productSlot.getNormedValue(), entranceModeSlot.getNormedValue());
                        productModePairList.add(productModePair);
                        slotList.remove(productSlot);
                        slotList.remove(entranceModeSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ACCOUNT_SUFFIX)) {
                        // slot模板同时抽取【产品名称】和【账号后缀】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot accountSuffixSlot = tmpSlotList.stream().filter(slot -> STR_ACCOUNT_SUFFIX.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productAccountSufixPair = new Pair<>(productSlot.getNormedValue(), accountSuffixSlot.getNormedValue());
                        productAccountSuffixPairList.add(productAccountSufixPair);
                        slotList.remove(productSlot);
                        slotList.remove(accountSuffixSlot);
                    }
                }
            });
            slotList.stream()
                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
                    .forEach(slot -> {
                        Pair<String, String> productModePair = new Pair<>(slot.getNormedValue(), null);
                        productModePairList.add(productModePair);
                    });
//            String entranceMode = slotList.stream()
//                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
//                    .map(slot -> slot.getNormedValue())
//                    .findFirst().orElse(null);
            List<String> taskNameEqualList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameNotContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameSuffixList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundStartSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundEndSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            String taskCreateTimeBoundStart = null;
            String taskCreateTimeBoundEnd = null;
            if (taskCreateTimeBoundPeriodSlot != null) {
                Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                if (pair != null) {
                    taskCreateTimeBoundStart = pair.getKey();
                    taskCreateTimeBoundEnd = pair.getValue();
//                    slotList.remove(taskCreateTimeBoundPeriodSlot);
                }
            }
            if (taskCreateTimeBoundStartSlot != null) {
                taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundStartSlot);
            }
            if (taskCreateTimeBoundEndSlot != null) {
                taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundEndSlot);
            }
            List<String> tenantLineList = slotList.stream()
                    .filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())
                            || STR_FILTERED_TENANT_LINE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            for (String tenantLine: tenantLineList) {
                tenantLinePairList.add(new Pair<>(tenantLine, null));
            }
            List<String> callingNumberList = slotList.stream()
                    .filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())
                            || STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            for (String callingNumber: callingNumberList) {
                tenantLinePairList.add(new Pair<>(null, callingNumber));
            }
            String expectedStartTime = slotList.stream()
                    .filter(slot -> STR_EXPECTED_START_TIME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String expectedEndTime = slotList.stream()
                    .filter(slot -> STR_EXPECTED_END_TIME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            TaskType taskType = slotList.stream()
                    .filter(slot -> STR_TASK_TYPE.equals(slot.getSlotName()))
                    .map(slot -> TaskType.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .findFirst().orElse(null);
            if (taskType == null) {
                taskType = TaskType.AI_AUTO;
            }
            Set<String> accountSet = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(HashSet::new));
            List<AccountProductInfo> accountProductInfoList = getAccountProductInfoList(accountSet, productAccountSuffixPairList, productModePairList);
            List<String> accountList = accountProductInfoList.stream().map(info -> info.getAccount()).collect(Collectors.toList());
//            InstructionType instructionType = InstructionType.ACTION_STOP_TASK;
            StopTaskInstructionBean instructionBean = new StopTaskInstructionBean(instructionId, chatGroup, creator, accountList,
                    productModePairList, taskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList,
                    taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLinePairList, expectedStartTime, expectedEndTime);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_RESUME_TASK)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            List<String> taskNameEqualList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameNotContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameSuffixList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundStartSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundEndSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            String taskCreateTimeBoundStart = null;
            String taskCreateTimeBoundEnd = null;
            if (taskCreateTimeBoundPeriodSlot != null) {
                Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                if (pair != null) {
                    taskCreateTimeBoundStart = pair.getKey();
                    taskCreateTimeBoundEnd = pair.getValue();
//                    slotList.remove(taskCreateTimeBoundPeriodSlot);
                }
            }
            if (taskCreateTimeBoundStartSlot != null) {
                taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundStartSlot);
            }
            if (taskCreateTimeBoundEndSlot != null) {
                taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundEndSlot);
            }
            List<TaskType> taskTypeList = slotList.stream()
                    .filter(slot -> STR_TASK_TYPE.equals(slot.getSlotName()))
                    .map(slot -> TaskType.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toList());
            List<TaskStatus> taskStatusList = slotList.stream()
                    .filter(slot -> STR_TASK_STATUS.equals(slot.getSlotName()))
                    .map(slot -> TaskStatus.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toList());
            Set<String> accountSet = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(HashSet::new));
            List<String> accountList = new ArrayList<>(accountSet);
            Boolean includeAutoStop = slotList.stream()
                    .filter(slot -> STR_IS_INCLUDE_AUTO_STOP.equals(slot.getSlotName()))
                    .map(slot -> STR_YES_SET.contains(slot.getNormedValue()) || !STR_NO_SET.contains(slot.getNormedValue()))
                    .findFirst().orElse(null);
            ResumeTaskInstructionBean instructionBean = new ResumeTaskInstructionBean(instructionId, chatGroup, creator, accountList,
                    taskTypeList, taskStatusList, includeAutoStop, taskNameEqualList, taskNameContainList, taskNameNotContainList,
                    taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_CHANGE_TENANT_LINE) || intentList.contains(STR_ACTION_CHANGE_CONCURRENCY) || intentList.contains(STR_ACTION_ADD_TASK)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            InstructionType instructionType;
            if (intentList.contains(STR_ACTION_CHANGE_TENANT_LINE)) {
                instructionType = InstructionType.ACTION_CHANGE_TENANT_LINE;
            } else if (intentList.contains(STR_ACTION_CHANGE_CONCURRENCY)) {
                instructionType = InstructionType.ACTION_CHANGE_CONCURRENCY;
            } else if (intentList.contains(STR_ACTION_ADD_TASK)) {
                instructionType = InstructionType.ACTION_ADD_TASK;
            } else {
                instructionType = null;
            }
            TaskType taskType = slotList.stream()
                    .filter(slot -> STR_TASK_TYPE.equals(slot.getSlotName()))
                    .map(slot -> TaskType.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .findFirst().orElse(null);
            if (taskType == null) {
                taskType = TaskType.AI_AUTO;
            }
            TaskType finalTaskType = taskType;

            String filteredTenantLine = slotList.stream()
                    .filter(slot -> STR_FILTERED_TENANT_LINE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);

            String filteredCallingNumber = slotList.stream()
                    .filter(slot -> STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);

            List<ChangeTenantLineInstructionBean> instructionBeanList = new ArrayList<>();
            List<Pair<String, String>> productModePairList = new ArrayList<>();
            List<Pair<String, String>> productAccountSuffixPairList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() > 2
                        && isContainSlot(tmpSlotList, STR_TENANT_LINE, 1)
                        && isContainSlot(tmpSlotList, STR_CALLING_NUMBER, tmpSlotList.size() - 2)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【线路方】、【主叫号码】+、【并发数】的
                    Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                    List<Slot> callingNumberSlotList = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).collect(Collectors.toList());
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();

//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualLisy = Collections.emptyList();
                    List<Slot> taskNameEqualSlotList = slotList.stream()
                            .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                            .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }
                    String tenantLine = lineSlot.getNormedValue();
                    String concurrency = concurrencySlot.getNormedValue();
                    String account = null;
                    String product = null;
                    String entranceMode = null;
                    for (Slot callingNumberSlot: callingNumberSlotList) {
                        String callingNumber = callingNumberSlot.getNormedValue();
                        ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                                instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, filteredTenantLine, filteredCallingNumber, tenantLine, callingNumber, concurrency, null, null, null);
                        instructionBeanList.add(instructionBean);
                    }
                    slotList.remove(lineSlot);
                    slotList.removeAll(callingNumberSlotList);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameEqualSlotList);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                } else if (tmpSlotList.size() > 2
                        && isContainSlot(tmpSlotList, STR_FILTERED_TENANT_LINE, 1)
                        && isContainSlot(tmpSlotList, STR_FILTERED_CALLING_NUMBER, tmpSlotList.size() - 2)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【筛选线路方】、【筛选主叫号码】+、【并发数】的
                    Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                    List<Slot> callingNumberSlotList = tmpSlotList.stream().filter(slot -> STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName())).collect(Collectors.toList());
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();

//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualLisy = Collections.emptyList();
                    List<Slot> taskNameEqualSlotList = slotList.stream()
                            .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                            .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);

                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }

                    String tenantLine = lineSlot.getNormedValue();
                    String concurrency = concurrencySlot.getNormedValue();
                    String account = null;
                    String product = null;
                    String entranceMode = null;
                    for (Slot callingNumberSlot: callingNumberSlotList) {
                        String callingNumber = callingNumberSlot.getNormedValue();
                        ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                                instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, tenantLine, callingNumber, concurrency, null, null, null);
                        instructionBeanList.add(instructionBean);
                    }
                    slotList.remove(lineSlot);
                    slotList.removeAll(callingNumberSlotList);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameEqualSlotList);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                } else if (tmpSlotList.size() > 2
                        && isContainSlot(tmpSlotList, STR_TASK_NAME_CONTAIN, tmpSlotList.size() - 2)
                        && isContainSlot(tmpSlotList, STR_TENANT_LINE, 1)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【任务名称包含】+、【线路方】、【并发数】的
                    Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                    Slot callingNumberSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_CALLING_NUMBER, slotGroupList).stream().findFirst().orElse(null);
                    String callingNumber = null;
                    if (callingNumberSlot != null) {
                        callingNumber = callingNumberSlot.getNormedValue();
                    }
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
                    List<String> taskNameEqualList = Collections.emptyList();
//                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<Slot> taskNameContainSlotList = tmpSlotList.stream().filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);
                    Slot expectedConnectedCallCountSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_CONNECTED_CALL_COUNT, slotGroupList).stream().findFirst().orElse(null);

                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }

                    String tenantLine = lineSlot.getNormedValue();
                    String concurrency = concurrencySlot.getNormedValue();
                    String account = null;
                    String product = null;
                    String entranceMode = null;
                    ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                            instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, filteredTenantLine, filteredCallingNumber, tenantLine, callingNumber, concurrency, null, null, null);
                    instructionBeanList.add(instructionBean);
                    slotList.remove(lineSlot);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                    slotList.remove(expectedConnectedCallCountSlot);
                } else if (isOnlyContainSlots(tmpSlotList, Sets.newHashSet(STR_TASK_NAME_CONTAIN, STR_TENANT_LINE, STR_CALLING_NUMBER,  STR_CONCURRENCY))
                        && isContainSlot(tmpSlotList, STR_TENANT_LINE, 1)
                        && isContainSlot(tmpSlotList, STR_CONCURRENCY, 1)) {
                    // slot模板同时抽取【任务名称包含】+、【线路方】、【主叫号码】+、【并发数】的
                    Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                    Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                    List<Slot> callingNumberSlotList = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).collect(Collectors.toList());
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
//                        List<String> taskNameEqualList = Collections.emptyList();
                    List<Slot> taskNameEqualSlotList = slotList.stream()
                            .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                            .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
//                    List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                    List<Slot> taskNameContainSlotList = tmpSlotList.stream().filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName())).collect(Collectors.toList());
                    List<String> taskNameContainList = taskNameContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                    List<String> taskNameNotContainList = taskNameNotContainSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                    List<String> taskNameSuffixList = taskNameSuffixSlotList
                            .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                    Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                    Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);

                    String taskCreateTimeBoundStart = null;
                    String taskCreateTimeBoundEnd = null;
                    if (taskCreateTimeBoundPeriodSlot != null) {
                        Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                        if (pair != null) {
                            taskCreateTimeBoundStart = pair.getKey();
                            taskCreateTimeBoundEnd = pair.getValue();
                            slotList.remove(taskCreateTimeBoundPeriodSlot);
                        }
                    }
                    if (taskCreateTimeBoundStartSlot != null) {
                        taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundStartSlot);
                    }
                    if (taskCreateTimeBoundEndSlot != null) {
                        taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                        slotList.remove(taskCreateTimeBoundEndSlot);
                    }

                    String tenantLine = lineSlot.getNormedValue();
                    String concurrency = concurrencySlot.getNormedValue();
                    String account = null;
                    String product = null;
                    String entranceMode = null;
                    String callingNumber = null;
                    ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                            instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, filteredTenantLine, filteredCallingNumber, tenantLine, callingNumber, concurrency, null, null, null);
                    instructionBeanList.add(instructionBean);

                    slotList.remove(lineSlot);
                    slotList.removeAll(callingNumberSlotList);
                    slotList.remove(concurrencySlot);
                    slotList.removeAll(taskNameEqualSlotList);
                    slotList.removeAll(taskNameContainSlotList);
                    slotList.removeAll(taskNameNotContainSlotList);
                    slotList.removeAll(taskNameSuffixSlotList);
                } else if (tmpSlotList.size() == 2) {
                    if (isContainSlot(tmpSlotList, STR_TENANT_LINE) && isContainSlot(tmpSlotList, STR_CONCURRENCY)) {
                        // slot模板同时抽取【线路方】和【并发数】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                        String tenantLine = lineSlot.getNormedValue();
                        String concurrency = concurrencySlot.getNormedValue();
                        String account = null;
                        String product = null;
                        String entranceMode = null;
                        String callingNumber = null;
                        List<String> taskNameEqualList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameContainList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameNotContainList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameSuffixList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                                .findFirst().orElse(null);
                        Slot taskCreateTimeBoundStartSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                                .findFirst().orElse(null);
                        Slot taskCreateTimeBoundEndSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                                .findFirst().orElse(null);

                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        if (taskCreateTimeBoundPeriodSlot != null) {
                            Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                            if (pair != null) {
                                taskCreateTimeBoundStart = pair.getKey();
                                taskCreateTimeBoundEnd = pair.getValue();
//                                slotList.remove(taskCreateTimeBoundPeriodSlot);
                            }
                        }
                        if (taskCreateTimeBoundStartSlot != null) {
                            taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                            slotList.remove(taskCreateTimeBoundStartSlot);
                        }
                        if (taskCreateTimeBoundEndSlot != null) {
                            taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                            slotList.remove(taskCreateTimeBoundEndSlot);
                        }
                        ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                                instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, filteredTenantLine, filteredCallingNumber, tenantLine, callingNumber, concurrency, null, null, null);
                        instructionBeanList.add(instructionBean);

                        slotList.remove(lineSlot);
                        slotList.remove(concurrencySlot);
                    } else if (isContainSlot(tmpSlotList, STR_FILTERED_TENANT_LINE) && isContainSlot(tmpSlotList, STR_CONCURRENCY)) {
                        // slot模板同时抽取【筛选线路方】和【并发数】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                        String tenantLine = lineSlot.getNormedValue();
                        String concurrency = concurrencySlot.getNormedValue();
                        String account = null;
                        String product = null;
                        String entranceMode = null;
                        String callingNumber = null;
                        List<String> taskNameEqualList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameContainList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameNotContainList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameSuffixList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                                .findFirst().orElse(null);
                        Slot taskCreateTimeBoundStartSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                                .findFirst().orElse(null);
                        Slot taskCreateTimeBoundEndSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                                .findFirst().orElse(null);

                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        if (taskCreateTimeBoundPeriodSlot != null) {
                            Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                            if (pair != null) {
                                taskCreateTimeBoundStart = pair.getKey();
                                taskCreateTimeBoundEnd = pair.getValue();
//                                slotList.remove(taskCreateTimeBoundPeriodSlot);
                            }
                        }
                        if (taskCreateTimeBoundStartSlot != null) {
                            taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                            slotList.remove(taskCreateTimeBoundStartSlot);
                        }
                        if (taskCreateTimeBoundEndSlot != null) {
                            taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                            slotList.remove(taskCreateTimeBoundEndSlot);
                        }
                        ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                                instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, tenantLine, callingNumber, concurrency, null, null, null);
                        instructionBeanList.add(instructionBean);

                        slotList.remove(lineSlot);
                        slotList.remove(concurrencySlot);
                    } else if (isContainSlot(tmpSlotList, STR_FILTERED_CALLING_NUMBER) && isContainSlot(tmpSlotList, STR_CONCURRENCY)) {
                        // slot模板同时抽取【筛选主叫号码】和【并发数】的
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        Slot concurrencySlot = tmpSlotList.stream().filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName())).findFirst().get();
                        String callingNumber = callingNumberSlot.getNormedValue();
                        String concurrency = concurrencySlot.getNormedValue();
                        String account = null;
                        String product = null;
                        String entranceMode = null;
                        String tenantLine = null;
                        List<String> taskNameEqualList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameContainList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameNotContainList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        List<String> taskNameSuffixList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                                .map(slot -> slot.getNormedValue())
                                .collect(Collectors.toList());
                        Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                                .findFirst().orElse(null);
                        Slot taskCreateTimeBoundStartSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                                .findFirst().orElse(null);
                        Slot taskCreateTimeBoundEndSlot = slotList.stream()
                                .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                                .findFirst().orElse(null);

                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        if (taskCreateTimeBoundPeriodSlot != null) {
                            Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                            if (pair != null) {
                                taskCreateTimeBoundStart = pair.getKey();
                                taskCreateTimeBoundEnd = pair.getValue();
//                                slotList.remove(taskCreateTimeBoundPeriodSlot);
                            }
                        }
                        if (taskCreateTimeBoundStartSlot != null) {
                            taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                            slotList.remove(taskCreateTimeBoundStartSlot);
                        }
                        if (taskCreateTimeBoundEndSlot != null) {
                            taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                            slotList.remove(taskCreateTimeBoundEndSlot);
                        }
                        ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                                instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, tenantLine, callingNumber, concurrency, null, null, null);
                        instructionBeanList.add(instructionBean);

                        slotList.remove(callingNumberSlot);
                        slotList.remove(concurrencySlot);
                    } else if (isContainSlot(tmpSlotList, STR_TENANT_LINE) && isContainSlot(tmpSlotList, STR_CALLING_NUMBER)) {
                        // slot模板同时抽取【线路方】和【主叫号码】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        String concurrency = null;
                        Slot concurrencySlot = getExclusiveSlotsForLineSlot(lineSlot, STR_EXPECTED_START_TIME, slotGroupList)
                                .stream().findFirst().orElse(null);
                        if (concurrencySlot != null) {
                            concurrency = concurrencySlot.getNormedValue();
                            slotList.remove(concurrencySlot);
                        }
//                        List<String> taskNameEqualList = Collections.emptyList();
//                        List<String> taskNameContainList = Collections.emptyList();
//                        List<String> taskNameNotContainList = Collections.emptyList();
                        List<Slot> taskNameEqualSlotList = slotList.stream()
                                .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName())).collect(Collectors.toList());
                        List<String> taskNameEqualList = taskNameEqualSlotList.stream()
                                .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_CONTAIN, slotGroupList);
                        List<String> taskNameContainList = taskNameContainSlotList.stream()
                                .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameNotContainSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_NOT_CONTAIN, slotGroupList);
                        List<String> taskNameNotContainList = taskNameNotContainSlotList.stream()
                                .map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        List<Slot> taskNameSuffixSlotList = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_NAME_SUFFIX, slotGroupList);
                        List<String> taskNameSuffixList = taskNameSuffixSlotList
                                .stream().map(slot -> slot.getNormedValue()).collect(Collectors.toList());
                        Slot taskCreateTimeBoundPeriodSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_PERIOD, slotGroupList).stream().findFirst().orElse(null);
                        Slot taskCreateTimeBoundStartSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_START, slotGroupList).stream().findFirst().orElse(null);
                        Slot taskCreateTimeBoundEndSlot = getExclusiveSlotsForLineSlot(lineSlot, STR_TASK_CREATE_TIME_BOUND_END, slotGroupList).stream().findFirst().orElse(null);

                        String taskCreateTimeBoundStart = null;
                        String taskCreateTimeBoundEnd = null;
                        if (taskCreateTimeBoundPeriodSlot != null) {
                            Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                            if (pair != null) {
                                taskCreateTimeBoundStart = pair.getKey();
                                taskCreateTimeBoundEnd = pair.getValue();
                                slotList.remove(taskCreateTimeBoundPeriodSlot);
                            }
                        }
                        if (taskCreateTimeBoundStartSlot != null) {
                            taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
                            slotList.remove(taskCreateTimeBoundStartSlot);
                        }
                        if (taskCreateTimeBoundEndSlot != null) {
                            taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
                            slotList.remove(taskCreateTimeBoundEndSlot);
                        }

                        String tenantLine = lineSlot.getNormedValue();
                        String callingNumber = callingNumberSlot.getNormedValue();
                        String account = null;
                        String product = null;
                        String entranceMode = null;
                        ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                                instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, filteredTenantLine, filteredCallingNumber, tenantLine, callingNumber, concurrency, null, null, null);
                        instructionBeanList.add(instructionBean);

                        slotList.remove(lineSlot);
                        slotList.remove(callingNumberSlot);
                        slotList.removeAll(taskNameEqualSlotList);
                        slotList.removeAll(taskNameContainSlotList);
                        slotList.removeAll(taskNameNotContainSlotList);
                        slotList.removeAll(taskNameSuffixSlotList);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ENTRANCE_MODE)) {
                        // slot模板同时抽取【产品名称】和【入口模式】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot entranceModeSlot = tmpSlotList.stream().filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productModePair = new Pair<>(productSlot.getNormedValue(), entranceModeSlot.getNormedValue());
                        productModePairList.add(productModePair);
                        slotList.remove(productSlot);
                        slotList.remove(entranceModeSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ACCOUNT_SUFFIX)) {
                        // slot模板同时抽取【产品名称】和【账号后缀】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot accountSuffixSlot = tmpSlotList.stream().filter(slot -> STR_ACCOUNT_SUFFIX.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productAccountSufixPair = new Pair<>(productSlot.getNormedValue(), accountSuffixSlot.getNormedValue());
                        productAccountSuffixPairList.add(productAccountSufixPair);
                        slotList.remove(productSlot);
                        slotList.remove(accountSuffixSlot);
                    }
                }
            });
            Set<String> accountSet = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(HashSet::new));
            slotList.stream()
                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
                    .forEach(slot -> {
                        Pair<String, String> productModePair = new Pair<>(slot.getNormedValue(), null);
                        productModePairList.add(productModePair);
                    });
            slotList.stream()
                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .distinct()
                    .forEach(entranceMode -> productModePairList.add(new Pair<>(null, entranceMode)));
            if (productModePairList.isEmpty()) {
                productModePairList.add(new Pair<>(null, null));
            }
//            String entranceMode = slotList.stream()
//                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
//                    .map(slot -> slot.getNormedValue())
//                    .findFirst().orElse(null);
            List<AccountProductInfo> accountProductInfoList = getAccountProductInfoList(accountSet, productAccountSuffixPairList, productModePairList);
            String account = accountProductInfoList.isEmpty()? null: accountProductInfoList.get(0).getAccount();
            String product = accountProductInfoList.isEmpty()? null: accountProductInfoList.get(0).getProduct();
            String entranceMode = accountProductInfoList.isEmpty()? null: accountProductInfoList.get(0).getEntranceMode();
//            List<String> accountList = accountProductInfoList.stream().map(info -> info.getAccount()).collect(Collectors.toList());
//            String account = accountList.isEmpty()? null: accountList.get(0);
//            String account = slotList.stream()
//                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
//                    .map(slot -> slot.getNormedValue())
//                    .findFirst().orElse(null);
//            String product = slotList.stream()
//                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
//                    .map(slot -> slot.getNormedValue())
//                    .findFirst().orElse(null);
//            String entranceMode = slotList.stream()
//                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
//                    .map(slot -> slot.getNormedValue())
//                    .findFirst().orElse(null);
            List<String> taskNameEqualList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameNotContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameSuffixList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundStartSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundEndSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                    .findFirst().orElse(null);

            String taskCreateTimeBoundStart = null;
            String taskCreateTimeBoundEnd = null;
            if (taskCreateTimeBoundPeriodSlot != null) {
                Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                if (pair != null) {
                    taskCreateTimeBoundStart = pair.getKey();
                    taskCreateTimeBoundEnd = pair.getValue();
//                    slotList.remove(taskCreateTimeBoundPeriodSlot);
                }
            }
            if (taskCreateTimeBoundStartSlot != null) {
                taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundStartSlot);
            }
            if (taskCreateTimeBoundEndSlot != null) {
                taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundEndSlot);
            }

            String tenantLine = slotList.stream()
                    .filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String callingNumber = slotList.stream()
                    .filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String concurrency = slotList.stream()
                    .filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String expectedStartTime = slotList.stream()
                    .filter(slot -> STR_EXPECTED_START_TIME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String expectedEndTime = slotList.stream()
                    .filter(slot -> STR_EXPECTED_END_TIME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String expectedConnectedCallCount = slotList.stream()
                    .filter(slot -> STR_EXPECTED_CONNECTED_CALL_COUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
//            List<AccountProductInfo> accountProductInfoList = getAccountProductInfoList(accountSet, productAccountSuffixPairList, productModePairList);
            if (!StringUtils.isEmpty(tenantLine) || !StringUtils.isEmpty(callingNumber) || !StringUtils.isEmpty(concurrency) || !StringUtils.isEmpty(expectedEndTime)) {
                // 如果多出来的这些参数，已识别的指令正好空着，则填到已识别指令里
                if (instructionBeanList.size() == 1
                        && (StringUtils.isEmpty(tenantLine) || StringUtils.isEmpty(instructionBeanList.get(0).getTenantLine()))
                        && (StringUtils.isEmpty(callingNumber) || StringUtils.isEmpty(instructionBeanList.get(0).getCallingNumber()))
                        && (StringUtils.isEmpty(concurrency) || StringUtils.isEmpty(instructionBeanList.get(0).getConcurrency()))
                        && (StringUtils.isEmpty(expectedEndTime) || StringUtils.isEmpty(instructionBeanList.get(0).getExpectedEndTime()))) {
                    ChangeTenantLineInstructionBean instructionBean = instructionBeanList.get(0);
                    if (!StringUtils.isEmpty(tenantLine)) {
                        instructionBean.setTenantLine(tenantLine);
                    }
                    if (!StringUtils.isEmpty(callingNumber)) {
                        instructionBean.setCallingNumber(callingNumber);
                    }
                    if (!StringUtils.isEmpty(concurrency)) {
                        instructionBean.setConcurrency(concurrency);
                    }
                    if (!StringUtils.isEmpty(expectedEndTime)) {
                        instructionBean.setExpectedEndTime(expectedEndTime);
                    }
                } else {
                    // 多出来的参数单独建条指令
                    ChangeTenantLineInstructionBean instructionBean = new ChangeTenantLineInstructionBean(
                            instructionId, instructionType, chatGroup, creator, account, product, entranceMode, finalTaskType, taskNameEqualList, taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, filteredTenantLine, filteredCallingNumber, tenantLine, callingNumber, concurrency, expectedStartTime, expectedEndTime, expectedConnectedCallCount);
                    instructionBeanList.add(instructionBean);
                }
            }

            for (ChangeTenantLineInstructionBean bean: instructionBeanList) {
                if (account != null && bean.getAccount() == null) {
                    bean.setAccount(account);
                }
                if (product != null && bean.getProduct() == null) {
                    bean.setProduct(product);
                }
                if (entranceMode != null && bean.getEntranceMode() == null) {
                    bean.setEntranceMode(entranceMode);
                }
                if (!CollectionUtils.isEmpty(taskNameEqualList) && CollectionUtils.isEmpty(bean.getTaskNameEqualList())) {
                    bean.setTaskNameEqualList(taskNameEqualList);
                }
                if (!CollectionUtils.isEmpty(taskNameContainList) && CollectionUtils.isEmpty(bean.getTaskNameContainList())) {
                    bean.setTaskNameContainList(taskNameContainList);
                }
                if (!CollectionUtils.isEmpty(taskNameNotContainList) && CollectionUtils.isEmpty(bean.getTaskNameNotContainList())) {
                    bean.setTaskNameNotContainList(taskNameNotContainList);
                }
                if (!CollectionUtils.isEmpty(taskNameSuffixList) && CollectionUtils.isEmpty(bean.getTaskNameSuffixList())) {
                    bean.setTaskNameSuffixList(taskNameSuffixList);
                }
                if (!StringUtils.isEmpty(taskCreateTimeBoundStart) && StringUtils.isEmpty(bean.getTaskCreateTimeBoundStart())) {
                    bean.setTaskCreateTimeBoundStart(taskCreateTimeBoundStart);
                }
                if (!StringUtils.isEmpty(taskCreateTimeBoundEnd) && StringUtils.isEmpty(bean.getTaskCreateTimeBoundEnd())) {
                    bean.setTaskCreateTimeBoundEnd(taskCreateTimeBoundEnd);
                }
                if (!StringUtils.isEmpty(expectedStartTime) && StringUtils.isEmpty(bean.getExpectedStartTime())) {
                    bean.setExpectedStartTime(expectedStartTime);
                }
                if (!StringUtils.isEmpty(expectedEndTime) && StringUtils.isEmpty(bean.getExpectedEndTime())) {
                    bean.setExpectedStartTime(expectedEndTime);
                }
                if (!StringUtils.isEmpty(expectedConnectedCallCount) && StringUtils.isEmpty(bean.getExpectedConnectedCallCount())) {
                    bean.setExpectedConnectedCallCount(expectedConnectedCallCount);
                }
            }

//            allInstructionBeanList.addAll(instructionBeanList);
            if (!CollectionUtils.isEmpty(instructionBeanList)) {
                OneByOneChangeTenantLineInstructionBean instructionBean = new OneByOneChangeTenantLineInstructionBean(instructionId, chatGroup, creator, instructionBeanList);
                allInstructionBeanList.add(instructionBean);
            }
        } else if (intentList.contains(STR_ACTION_SET_LINE_RATIO)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

//            List<String> accountList = null;
            List<Pair<String, String>> productModePairList = new ArrayList<>();
            List<Pair<String, String>> productAccountSuffixPairList = new ArrayList<>();
            List<Pair<String, String>> tenantLinePairList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() == 2) {
                    if (isContainSlot(tmpSlotList, STR_FILTERED_TENANT_LINE) && isContainSlot(tmpSlotList, STR_FILTERED_CALLING_NUMBER)) {
                        // slot模板同时抽取【筛选线路方】和【筛选主叫号码】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        String tenantLine = lineSlot.getNormedValue();
                        String callingNumber = callingNumberSlot.getNormedValue();

                        tenantLinePairList.add(new Pair<>(tenantLine, callingNumber));

                        slotList.remove(lineSlot);
                        slotList.remove(callingNumberSlot);
                    } else if (isContainSlot(tmpSlotList, STR_TENANT_LINE) && isContainSlot(tmpSlotList, STR_CALLING_NUMBER)) {
                        // slot模板同时抽取【线路方】和【主叫号码】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        String tenantLine = lineSlot.getNormedValue();
                        String callingNumber = callingNumberSlot.getNormedValue();

                        tenantLinePairList.add(new Pair<>(tenantLine, callingNumber));

                        slotList.remove(lineSlot);
                        slotList.remove(callingNumberSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ENTRANCE_MODE)) {
                        // slot模板同时抽取【产品名称】和【入口模式】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot entranceModeSlot = tmpSlotList.stream().filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productModePair = new Pair<>(productSlot.getNormedValue(), entranceModeSlot.getNormedValue());
                        productModePairList.add(productModePair);
                        slotList.remove(productSlot);
                        slotList.remove(entranceModeSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ACCOUNT_SUFFIX)) {
                        // slot模板同时抽取【产品名称】和【账号后缀】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot accountSuffixSlot = tmpSlotList.stream().filter(slot -> STR_ACCOUNT_SUFFIX.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productAccountSufixPair = new Pair<>(productSlot.getNormedValue(), accountSuffixSlot.getNormedValue());
                        productAccountSuffixPairList.add(productAccountSufixPair);
                        slotList.remove(productSlot);
                        slotList.remove(accountSuffixSlot);
                    }
                }
            });
            slotList.stream()
                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
                    .forEach(slot -> {
                        Pair<String, String> productModePair = new Pair<>(slot.getNormedValue(), null);
                        productModePairList.add(productModePair);
                    });
//            String entranceMode = slotList.stream()
//                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
//                    .map(slot -> slot.getNormedValue())
//                    .findFirst().orElse(null);
            List<String> taskNameEqualList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameNotContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameSuffixList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundStartSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundEndSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                    .findFirst().orElse(null);

            String taskCreateTimeBoundStart = null;
            String taskCreateTimeBoundEnd = null;
            if (taskCreateTimeBoundPeriodSlot != null) {
                Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                if (pair != null) {
                    taskCreateTimeBoundStart = pair.getKey();
                    taskCreateTimeBoundEnd = pair.getValue();
//                    slotList.remove(taskCreateTimeBoundPeriodSlot);
                }
            }
            if (taskCreateTimeBoundStartSlot != null) {
                taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundStartSlot);
            }
            if (taskCreateTimeBoundEndSlot != null) {
                taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundEndSlot);
            }

            List<String> tenantLineList = slotList.stream()
                    .filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())
                            || STR_FILTERED_TENANT_LINE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            for (String tenantLine: tenantLineList) {
                tenantLinePairList.add(new Pair<>(tenantLine, null));
            }
            List<String> callingNumberList = slotList.stream()
                    .filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())
                            || STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            for (String callingNumber: callingNumberList) {
                tenantLinePairList.add(new Pair<>(null, callingNumber));
            }
            if (CollectionUtils.isEmpty(tenantLinePairList)) {
                tenantLinePairList.add(new Pair<>(null, null));
            }
            String lineRatio = slotList.stream()
                    .filter(slot -> STR_LINE_RATIO.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Set<String> accountSet = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(HashSet::new));
            List<AccountProductInfo> accountProductInfoList = getAccountProductInfoList(accountSet, productAccountSuffixPairList, productModePairList);
//            InstructionType instructionType = InstructionType.ACTION_SET_LINE_RATIO;
            for (AccountProductInfo info: accountProductInfoList) {
                String account = info.getAccount();
                String product = info.getProduct();
                String entranceMode = info.getEntranceMode();
                for (Pair<String, String> pair: tenantLinePairList) {
                    String tenantLine = pair.getKey();
                    String callingNumber = pair.getValue();
                    SetLineRatioInstructionBean instructionBean = new SetLineRatioInstructionBean(instructionId, chatGroup,
                            creator, account, product, entranceMode, taskNameEqualList, taskNameContainList, taskNameNotContainList,
                            taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd, tenantLine, callingNumber, lineRatio);
                    allInstructionBeanList.add(instructionBean);
                }
            }
        } else if (intentList.contains(STR_ACTION_EDIT_SUPPLY_LINE)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            String filteredTenantLine = slotList.stream()
                    .filter(slot -> STR_FILTERED_TENANT_LINE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String filteredCallingNumber = slotList.stream()
                    .filter(slot -> STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String concurrency = slotList.stream()
                    .filter(slot -> STR_CONCURRENCY.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            EditSupplyLineInstructionBean instructionBean = new EditSupplyLineInstructionBean(instructionId, chatGroup, creator, filteredTenantLine, filteredCallingNumber, concurrency);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_EDIT_TENANT_LINE)) {
            String query = queryInfo.getRawQuery();
            query = query.replace("分省线路列表", "分地区线路列表");
            try {
                List<EditSingleTenantLineInstructionBean> singleInstructionBeanList = new ArrayList<>();
                JsonObject baseObj = JsonUtils.parseJson(query).getAsJsonObject();
                String json = baseObj.get("操作内容").toString();
                List<CustomTenantLineInfo> customTenantLineInfoList = JsonUtils.fromJson(json, new TypeToken<List<CustomTenantLineInfo>>() {
                });
                for (CustomTenantLineInfo customInfo : customTenantLineInfoList) {
                    String account = customInfo.getAccount();
                    String filteredTenantLine = customInfo.getTenantLineName();
                    EnableStatus enableStatus = EnableStatus.fromCaption(customInfo.getLineStatus());
                    String maxConcurrency = (customInfo.getMaxConcurrency() == null) ? null : String.valueOf(customInfo.getMaxConcurrency());
                    Set<String> secondIndustrySet = (CollectionUtils.isEmpty(customInfo.getSecondIndustryList())) ? null : new HashSet<>(customInfo.getSecondIndustryList());
                    List<CustomSupplyLineGroup> customGroupList = customInfo.getSupplyLineGroupList();
                    List<DesignedSupplyLineGroup> designedGroupList = customGroupList.stream()
                            .map(customGroup -> new DesignedSupplyLineGroup(customGroup)).collect(Collectors.toList());
                    EditSingleTenantLineInstructionBean instructionBean = new EditSingleTenantLineInstructionBean(instructionId, chatGroup,
                            creator, account, filteredTenantLine, null, enableStatus, maxConcurrency, secondIndustrySet, designedGroupList);
                    singleInstructionBeanList.add(instructionBean);
                }
                EditTenantLineInstructionBean instructionBean = new EditTenantLineInstructionBean(instructionId, chatGroup, creator, singleInstructionBeanList);
                allInstructionBeanList.add(instructionBean);
            } catch (IllegalStateException e) {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }  else if (intentList.contains(STR_ACTION_DOWNLOAD_AUDIO)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            List<Pair<String, String>> productModePairList = new ArrayList<>();
            List<Pair<String, String>> productAccountSuffixPairList = new ArrayList<>();
            List<Pair<String, String>> tenantLinePairList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() == 2) {
                    if (isContainSlot(tmpSlotList, STR_FILTERED_TENANT_LINE) && isContainSlot(tmpSlotList, STR_FILTERED_CALLING_NUMBER)) {
                        // slot模板同时抽取【筛选线路方】和【筛选主叫号码】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        String tenantLine = lineSlot.getNormedValue();
                        String callingNumber = callingNumberSlot.getNormedValue();

                        tenantLinePairList.add(new Pair<>(tenantLine, callingNumber));

                        slotList.remove(lineSlot);
                        slotList.remove(callingNumberSlot);
                    } else if (isContainSlot(tmpSlotList, STR_TENANT_LINE) && isContainSlot(tmpSlotList, STR_CALLING_NUMBER)) {
                        // slot模板同时抽取【线路方】和【主叫号码】的
                        Slot lineSlot = tmpSlotList.stream().filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())).findFirst().get();
                        Slot callingNumberSlot = tmpSlotList.stream().filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())).findFirst().get();
                        String tenantLine = lineSlot.getNormedValue();
                        String callingNumber = callingNumberSlot.getNormedValue();

                        tenantLinePairList.add(new Pair<>(tenantLine, callingNumber));

                        slotList.remove(lineSlot);
                        slotList.remove(callingNumberSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ENTRANCE_MODE)) {
                        // slot模板同时抽取【产品名称】和【入口模式】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot entranceModeSlot = tmpSlotList.stream().filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productModePair = new Pair<>(productSlot.getNormedValue(), entranceModeSlot.getNormedValue());
                        productModePairList.add(productModePair);
                        slotList.remove(productSlot);
                        slotList.remove(entranceModeSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ACCOUNT_SUFFIX)) {
                        // slot模板同时抽取【产品名称】和【账号后缀】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot accountSuffixSlot = tmpSlotList.stream().filter(slot -> STR_ACCOUNT_SUFFIX.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productAccountSufixPair = new Pair<>(productSlot.getNormedValue(), accountSuffixSlot.getNormedValue());
                        productAccountSuffixPairList.add(productAccountSufixPair);
                        slotList.remove(productSlot);
                        slotList.remove(accountSuffixSlot);
                    }
                }
            });
            Set<String> accountSet = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(HashSet::new));
            slotList.stream()
                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
                    .forEach(slot -> {
                        Pair<String, String> productModePair = new Pair<>(slot.getNormedValue(), null);
                        productModePairList.add(productModePair);
                    });
            slotList.stream()
                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .distinct()
                    .forEach(entranceMode -> productModePairList.add(new Pair<>(null, entranceMode)));
            if (productModePairList.isEmpty()) {
                productModePairList.add(new Pair<>(null, null));
            }
            List<AccountProductInfo> accountProductInfoList = getAccountProductInfoList(accountSet, productAccountSuffixPairList, productModePairList);
            String account = accountProductInfoList.isEmpty()? null: accountProductInfoList.get(0).getAccount();
            String product = accountProductInfoList.isEmpty()? null: accountProductInfoList.get(0).getProduct();
            String entranceMode = accountProductInfoList.isEmpty()? null: accountProductInfoList.get(0).getEntranceMode();

            List<String> tenantLineList = slotList.stream()
                    .filter(slot -> STR_TENANT_LINE.equals(slot.getSlotName())
                            || STR_FILTERED_TENANT_LINE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            for (String tenantLine: tenantLineList) {
                tenantLinePairList.add(new Pair<>(tenantLine, null));
            }
            List<String> callingNumberList = slotList.stream()
                    .filter(slot -> STR_CALLING_NUMBER.equals(slot.getSlotName())
                            || STR_FILTERED_CALLING_NUMBER.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            for (String callingNumber: callingNumberList) {
                tenantLinePairList.add(new Pair<>(null, callingNumber));
            }
            if (CollectionUtils.isEmpty(tenantLinePairList)) {
                tenantLinePairList.add(new Pair<>(null, null));
            }

            Set<TaskType> taskTypeSet = slotList.stream()
                    .filter(slot -> STR_TASK_TYPE.equals(slot.getSlotName()))
                    .map(slot -> TaskType.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (tenantLinePairList.size() == 1) {
                // 只有1条线路
                Pair<String, String> pair = tenantLinePairList.get(0);
                String tenantLine = pair.getKey();
                String callingNumber = pair.getValue();
                Set<Slot> phoneSlotSet = slotList.stream()
                        .filter(slot -> STR_PHONE.equals(slot.getSlotName()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (phoneSlotSet.size() == 1) {
                    // 只要1条线路、1个电话号码
                    String phone = phoneSlotSet.stream().map(slot -> slot.getNormedValue()).findFirst().get();
                    String year = slotList.stream()
                            .filter(slot -> STR_YEAR.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().orElse(null);
                    String month = slotList.stream()
                            .filter(slot -> STR_MONTH.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().orElse(null);
                    String day = slotList.stream()
                            .filter(slot -> STR_DAY.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().orElse(null);
                    String time = slotList.stream()
                            .filter(slot -> STR_TIME.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().orElse(null);
                    String strDate = slotList.stream()
                            .filter(slot -> STR_DATE.equals(slot.getSlotName()))
                            .map(slot -> slot.getNormedValue())
                            .findFirst().orElse(null);
                    if (strDate == null) {
                        strDate = getStrDate(year, month, day);
                    }
                    String datetime = strDate;
                    if (!StringUtils.isEmpty(time)) {
                        datetime = datetime + " " + time;
                    }
                    DownloadAudioInstructionBean bean = new DownloadAudioInstructionBean(instructionId, chatGroup, creator, account,
                            product, entranceMode, taskTypeSet, phone, datetime, tenantLine, callingNumber);
                    allInstructionBeanList.add(bean);
                } else { // 1条线路、多个电话号码
                    for (Slot phoneSlot : phoneSlotSet) {
                        String phone = phoneSlot.getNormedValue();
                        String year = getExclusiveSlotsForLineSlot(phoneSlot, STR_YEAR, slotGroupList).
                                stream().map(slot -> slot.getNormedValue()).findFirst().orElse(null);
                        String month = getExclusiveSlotsForLineSlot(phoneSlot, STR_MONTH, slotGroupList).stream()
                                .map(slot -> slot.getNormedValue()).findFirst().orElse(null);
                        String day = getExclusiveSlotsForLineSlot(phoneSlot, STR_DAY, slotGroupList).stream()
                                .map(slot -> slot.getNormedValue()).findFirst().orElse(null);
                        String time = getExclusiveSlotsForLineSlot(phoneSlot, STR_TIME, slotGroupList).stream()
                                .map(slot -> slot.getNormedValue()).findFirst().orElse(null);

                        String strDate = getExclusiveSlotsForLineSlot(phoneSlot, STR_DATE, slotGroupList).
                                stream().map(slot -> slot.getNormedValue()).findFirst().orElse(null);
                        if (strDate == null) {
                            if (day != null) {
                                strDate = getStrDate(year, month, day);
                            } else {
                                strDate = slotList.stream()
                                        .filter(slot -> STR_DATE.equals(slot.getSlotName()))
                                        .map(slot -> slot.getNormedValue())
                                        .findFirst().orElse(null);
                            }
                        }
                        String datetime = strDate;
                        if (!StringUtils.isEmpty(time)) {
                            datetime = datetime + " " + time;
                        }
                        DownloadAudioInstructionBean bean = new DownloadAudioInstructionBean(instructionId, chatGroup, creator, account,
                                product, entranceMode, taskTypeSet, phone, datetime, tenantLine, callingNumber);
                        allInstructionBeanList.add(bean);
                    }
                }
            }
        } else if (intentList.contains(STR_ACTION_CREATE_MAIN_ACCOUNT)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            List<AccountInfo> accountInfoList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() >= 2) {
                    if (isContainSlot(tmpSlotList, STR_ROLE_NAME)
                            && getSlotCount(tmpSlotList, STR_ACCOUNT_INFO) == tmpSlotList.size() - 1) {
                        Slot subRoleNameSlot = tmpSlotList.stream()
                                .filter(slot -> STR_ROLE_NAME.equals(slot.getSlotName())).findFirst().get();
                        String subRoleName = subRoleNameSlot.getNormedValue();
                        Role subRole = Role.fromCaption(subRoleName);
                        if (subRole != null) {
                            List<Slot> accountInfoSlotList = tmpSlotList.stream()
                                    .filter(slot -> STR_ACCOUNT_INFO.equals(slot.getSlotName())).collect(Collectors.toList());
                            for (Slot accountInfoSlot: accountInfoSlotList) {
                                String[] items = accountInfoSlot.getNormedValue().split("/");
                                String subAccount = (items.length >= 1) ? items[0]: null;
                                String subPassword = (items.length >= 2) ? items[1]: null;
                                String subName = (items.length >= 3) ? items[2]: null;
                                AccountInfo accountInfo = new AccountInfo(subAccount, subPassword, subName, subRoleName);
                                accountInfoList.add(accountInfo);
                            }
                        }
                    }
                }
            });
            String tenantName = slotList.stream()
                    .filter(slot -> STR_TENANT_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String mainAccount = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String mainPassword = slotList.stream()
                    .filter(slot -> STR_PASSWORD.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String mainContacts = slotList.stream()
                    .filter(slot -> STR_CONTACTS.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            PhoneNumType phoneNumType = slotList.stream()
                    .filter(slot -> STR_PHONE_NUM_TYPE.equals(slot.getSlotName()))
                    .map(slot -> PhoneNumType.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .findFirst().orElse(PhoneNumType.PLAINTEXT_ALL_THE_TIME);
            String productName = slotList.stream()
                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String secondIndustryName = slotList.stream()
                    .filter(slot -> STR_SECOND_INDUSTRY_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Set<String> baizeIpSet = slotList.stream()
                    .filter(slot -> STR_BAIZE_IP.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<String> jiafangIpSet = slotList.stream()
                    .filter(slot -> STR_JIAFANG_IP.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
//            InstructionType instructionType = InstructionType.ACTION_CREATE_MAIN_ACCOUNT;
            AbstractInstructionBean instructionBean = new CreateMainAccountInstructionBean(instructionId, chatGroup, creator, tenantName, mainAccount, mainPassword, mainContacts, phoneNumType, productName, secondIndustryName, accountInfoList, baizeIpSet, jiafangIpSet);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_CREATE_SUB_ACCOUNT)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            List<AccountInfo> accountInfoList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() >= 2) {
                    if (isContainSlot(tmpSlotList, STR_ROLE_NAME)
                            && getSlotCount(tmpSlotList, STR_ACCOUNT_INFO) == tmpSlotList.size() - 1) {
                        Slot subRoleNameSlot = tmpSlotList.stream()
                                .filter(slot -> STR_ROLE_NAME.equals(slot.getSlotName())).findFirst().get();
                        String subRoleName = subRoleNameSlot.getNormedValue();
                        List<Slot> accountInfoSlotList = tmpSlotList.stream()
                                .filter(slot -> STR_ACCOUNT_INFO.equals(slot.getSlotName())).collect(Collectors.toList());
                        for (Slot accountInfoSlot: accountInfoSlotList) {
                            String[] items = accountInfoSlot.getNormedValue().split("/");
                            String subAccount = (items.length >= 1) ? items[0]: null;
                            String subPassword = (items.length >= 2) ? items[1]: null;
                            String subName = (items.length >= 3) ? items[2]: null;
                            AccountInfo accountInfo = new AccountInfo(subAccount, subPassword, subName, subRoleName);
                            accountInfoList.add(accountInfo);
                        }
                    }
                }
            });
            String mainAccount = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
//            InstructionType instructionType = InstructionType.ACTION_CREATE_SUB_ACCOUNT;
            AbstractInstructionBean instructionBean = new CreateSubAccountInstructionBean(instructionId, chatGroup, creator, mainAccount, accountInfoList);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_CREATE_ROLE)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            String mainAccount = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String roleName = slotList.stream()
                    .filter(slot -> STR_ROLE_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Role sourceRole = slotList.stream()
                    .filter(slot -> STR_SOURCE_ROLE_NAME.equals(slot.getSlotName()))
                    .map(slot -> Role.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .findFirst().orElse(null);
            Set<String> ipSet = slotList.stream()
                    .filter(slot -> STR_IP.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
//            InstructionType instructionType = InstructionType.ACTION_CREATE_ROLE;
            AbstractInstructionBean instructionBean = new CreateRoleInstructionBean(instructionId, chatGroup, creator, mainAccount, roleName, sourceRole, ipSet);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_ADD_ROLE_IP)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            String mainAccount = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String roleName = slotList.stream()
                    .filter(slot -> STR_ROLE_NAME.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Set<String> ipSet = slotList.stream()
                    .filter(slot -> STR_IP.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
//            InstructionType instructionType = InstructionType.ACTION_ADD_ROLE_IP;
            AbstractInstructionBean instructionBean = new AddRoleIpInstructionBean(instructionId, chatGroup, creator, mainAccount, roleName, ipSet);
            allInstructionBeanList.add(instructionBean);
        } else if (intentList.contains(STR_ACTION_SET_ACCOUNT_OPERATOR_PARAM)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);
            String account = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Set<OutboundType> dataStatisticOutboundTypeSet = slotList.stream()
                    .filter(slot -> STR_DATA_STATISTIC_OUTBOUND_TYPE.equals(slot.getSlotName()))
                    .map(slot -> OutboundType.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            String taskCallbackUrl = slotList.stream()
                    .filter(slot -> STR_TASK_CALLBACK_URL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Set<OutboundType> callbackOutboundTypeSet = slotList.stream()
                    .filter(slot -> STR_CALLBACK_OUTBOUND_TYPE.equals(slot.getSlotName()))
                    .map(slot -> OutboundType.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<CallbackStatus> callbackStatusSet = slotList.stream()
                    .filter(slot -> STR_CALLBACK_STATUS.equals(slot.getSlotName()))
                    .map(slot -> CallbackStatus.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<OutboundCallbackField> outboundCallbackFieldSet = slotList.stream()
                    .filter(slot -> STR_OUTBOUND_CALLBACK_FIELD.equals(slot.getSlotName()))
                    .map(slot -> OutboundCallbackField.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            String quicklyCallbackUrl = slotList.stream()
                    .filter(slot -> STR_QUICKLY_CALLBACK_URL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String newCallbackUrl = slotList.stream()
                    .filter(slot -> STR_NEW_CALLBACK_URL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String txtUpdateCallbackUrl = slotList.stream()
                    .filter(slot -> STR_TXT_UPDATE_CALLBACK_URL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String mSmsCallbackUrl = slotList.stream()
                    .filter(slot -> STR_M_SMS_CALLBACK_URL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String oldCallBackUrl = slotList.stream()
                    .filter(slot -> STR_OLD_CALLBACK_URL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Set<SmsCallbackField> smsCallbackFieldSet = slotList.stream()
                    .filter(slot -> STR_SMS_CALLBACK_FIELD.equals(slot.getSlotName()))
                    .map(slot -> SmsCallbackField.fromCaption(slot.getNormedValue()))
                    .filter(x -> x != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            String smsCallbackUrl = slotList.stream()
                    .filter(slot -> STR_SMS_CALLBACK_URL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            String upSmsCallbackUrl = slotList.stream()
                    .filter(slot -> STR_UP_SMS_CALLBACK_URL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .findFirst().orElse(null);
            Set<String> ipSet = slotList.stream()
                    .filter(slot -> STR_IP.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            AbstractInstructionBean instructionBean = new SetAccountOperatorParamInstructionBean(
                    instructionId, chatGroup, creator, account, dataStatisticOutboundTypeSet, taskCallbackUrl,
                    callbackOutboundTypeSet, callbackStatusSet, outboundCallbackFieldSet, quicklyCallbackUrl,
                    newCallbackUrl, txtUpdateCallbackUrl, mSmsCallbackUrl, oldCallBackUrl, smsCallbackFieldSet,
                    smsCallbackUrl, upSmsCallbackUrl, ipSet);
            allInstructionBeanList.add(instructionBean);
        }
        if (intentList.contains(STR_ACTION_FORBID_DISTRICT) || intentList.contains(STR_ACTION_ALLOW_DISTRICT)) {
            List<Slot> slotList = new ArrayList<>(allSlotList);

            List<Pair<String, String>> productModePairList = new ArrayList<>();
            List<Pair<String, String>> productAccountSuffixPairList = new ArrayList<>();
            source2slotList.forEach((source, tmpSlotList) -> {
                if (tmpSlotList.size() == 2) {
                    if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ENTRANCE_MODE)) {
                        // slot模板同时抽取【产品名称】和【入口模式】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot entranceModeSlot = tmpSlotList.stream().filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName())).findFirst().get();
                        String product = productSlot.getNormedValue();
                        String entranceMode = entranceModeSlot.getNormedValue();
                        productModePairList.add(new Pair<>(product, entranceMode));
                        slotList.remove(productSlot);
                        slotList.remove(entranceModeSlot);
                    } else if (isContainSlot(tmpSlotList, STR_PRODUCT) && isContainSlot(tmpSlotList, STR_ACCOUNT_SUFFIX)) {
                        // slot模板同时抽取【产品名称】和【账号后缀】的
                        Slot productSlot = tmpSlotList.stream().filter(slot -> STR_PRODUCT.equals(slot.getSlotName())).findFirst().get();
                        Slot accountSuffixSlot = tmpSlotList.stream().filter(slot -> STR_ACCOUNT_SUFFIX.equals(slot.getSlotName())).findFirst().get();
                        Pair<String, String> productAccountSufixPair = new Pair<>(productSlot.getNormedValue(), accountSuffixSlot.getNormedValue());
                        productAccountSuffixPairList.add(productAccountSufixPair);
                        slotList.remove(productSlot);
                        slotList.remove(accountSuffixSlot);
                    }
                }
            });
            Set<DistrictBean> forbiddenProvinceSet = new LinkedHashSet<>();
            List<Slot> provinceSlotList = slotList.stream()
                    .filter(slot -> STR_FORBIDDEN_PROVINCE.equals(slot.getSlotName())
                            || STR_PROVINCE.equals(slot.getSlotName()))
                    .collect(Collectors.toList());
            for (Slot slot: provinceSlotList) {
                String provinceName = slot.getNormedValue();
                String provinceCode = DistrictUtils.getProvinceCode(provinceName);
                DistrictBean districtBean = new DistrictBean(provinceName, provinceCode);
                forbiddenProvinceSet.add(districtBean);
//                ForbiddenInfoBean forbiddenInfoBean = new ForbiddenInfoBean(districtBean, Operator.getAllOperatorList());
//                forbiddenProvinceList.add(forbiddenInfoBean);
//                slotList.remove(slot);
            }

//            List<ForbiddenInfoBean> forbiddenCityList = new ArrayList<>();
            Set<DistrictBean> forbiddenCitySet = new LinkedHashSet<>();
            List<Slot> citySlotList = slotList.stream()
                    .filter(slot -> STR_FORBIDDEN_CITY.equals(slot.getSlotName())
                            || STR_CITY.equals(slot.getSlotName()))
                    .collect(Collectors.toList());
            for (Slot slot: citySlotList) {
                String cityName = slot.getNormedValue();
                String cityCode = DistrictUtils.getCityCode(cityName);
                DistrictBean districtBean = new DistrictBean(cityName, cityCode);
                forbiddenCitySet.add(districtBean);
//                ForbiddenInfoBean forbiddenInfoBean = new ForbiddenInfoBean(districtBean, Operator.getAllOperatorList());
//                forbiddenCityList.add(forbiddenInfoBean);
//                slotList.remove(slot);
            }

            slotList.stream()
                    .filter(slot -> STR_PRODUCT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .distinct()
                    .forEach(product -> productModePairList.add(new Pair<>(product, null)));
            slotList.stream()
                    .filter(slot -> STR_ENTRANCE_MODE.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .distinct()
                    .forEach(entranceMode -> productModePairList.add(new Pair<>(null, entranceMode)));
            Set<String> accountSet = slotList.stream()
                    .filter(slot -> STR_ACCOUNT.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toCollection(HashSet::new));
            List<AccountProductInfo> accountProductInfoList = getAccountProductInfoList(accountSet, productAccountSuffixPairList, productModePairList);
            if (accountProductInfoList.isEmpty()) {
                accountProductInfoList.add(new AccountProductInfo(null, null, null));
            }

            List<String> taskNameEqualList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_EQUAL.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameNotContainList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_NOT_CONTAIN.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            List<String> taskNameSuffixList = slotList.stream()
                    .filter(slot -> STR_TASK_NAME_SUFFIX.equals(slot.getSlotName()))
                    .map(slot -> slot.getNormedValue())
                    .collect(Collectors.toList());
            Slot taskCreateTimeBoundPeriodSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_PERIOD.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundStartSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_START.equals(slot.getSlotName()))
                    .findFirst().orElse(null);
            Slot taskCreateTimeBoundEndSlot = slotList.stream()
                    .filter(slot -> STR_TASK_CREATE_TIME_BOUND_END.equals(slot.getSlotName()))
                    .findFirst().orElse(null);

            String taskCreateTimeBoundStart = null;
            String taskCreateTimeBoundEnd = null;
            if (taskCreateTimeBoundPeriodSlot != null) {
                Pair<String, String> pair = getTaskCreateTimeBoundPair(taskCreateTimeBoundPeriodSlot);
                if (pair != null) {
                    taskCreateTimeBoundStart = pair.getKey();
                    taskCreateTimeBoundEnd = pair.getValue();
//                    slotList.remove(taskCreateTimeBoundPeriodSlot);
                }
            }
            if (taskCreateTimeBoundStartSlot != null) {
                taskCreateTimeBoundStart = taskCreateTimeBoundStartSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundStartSlot);
            }
            if (taskCreateTimeBoundEndSlot != null) {
                taskCreateTimeBoundEnd = taskCreateTimeBoundEndSlot.getNormedValue();
//                slotList.remove(taskCreateTimeBoundEndSlot);
            }

            boolean effectiveAllDay = slotList.stream()
                    .filter(slot -> STR_EFFECTIVE_ALL_DAY.equals(slot.getSlotName()))
                    .map(slot -> STR_YES_SET.contains(slot.getNormedValue()))
                    .findFirst().orElse(true); // 默认全天生效
            InstructionType instructionType;
            if (intentList.contains(STR_ACTION_FORBID_DISTRICT)) {
                instructionType = InstructionType.ACTION_FORBID_DISTRICT;
            } else {
                instructionType = InstructionType.ACTION_ALLOW_DISTRICT;
            }

            for (AccountProductInfo info: accountProductInfoList) {
                String account = info.getAccount();
                String product = info.getProduct();
                String entranceMode = info.getEntranceMode();
                ForbidDistrictInstructionBean instructionBean = new ForbidDistrictInstructionBean(
                        instructionId, instructionType, chatGroup, creator, account, product, entranceMode, taskNameEqualList,
                        taskNameContainList, taskNameNotContainList, taskNameSuffixList, taskCreateTimeBoundStart, taskCreateTimeBoundEnd,
                        forbiddenProvinceSet, forbiddenCitySet, effectiveAllDay);
                allInstructionBeanList.add(instructionBean);
            }
        }
        return allInstructionBeanList;
    }

    private static List<Slot> postprocess(List<Slot> requiredSlotList) {
        List<Slot> slotList = new ArrayList<>(requiredSlotList);
        // 根据主叫生成线路方slot
        if (isContainSlot(slotList, STR_CALLING_NUMBER) && !isContainSlot(slotList, STR_TENANT_LINE)) {
            List<Slot> callingNumberSlotList = slotList.stream()
                    .filter(x -> x.getSlotName().equals(STR_CALLING_NUMBER)).collect(Collectors.toList());
            for (Slot callNumberSlot: callingNumberSlotList) {
                String normedValue = callNumberSlot.getNormedValue();
                int start = callNumberSlot.getStartIndex();
                int end = callNumberSlot.getEndIndex();
                String evidence = callNumberSlot.getEvidence();
                if (normedValue.matches("^jr(yi|er|san|si|wu|zy|dt)[0-9]+$")) {
                    Slot slot = new Slot(STR_TENANT_LINE, "仙人", "仙人", start, end, 1.0f, false,"InstructionPostprocess", evidence);
                    slotList.add(slot);
                } else if (normedValue.matches("^jr[0-9]+$")) {
                    Slot slot = new Slot(STR_TENANT_LINE, "限时", "限时", start, end, 1.0f, false,"InstructionPostprocess", evidence);
                    slotList.add(slot);
                }
//                else {
//                    Slot slot = new Slot(STR_TENANT_LINE, "得心", "得心", start, end, 1.0f, "InstructionPostprocess", evidence);
//                    slotList.add(slot);
//                }
            }
        }

        // 将任务名称列表拆出来
        List<Slot> taskNameListSlotList = slotList.stream()
                .filter(x -> x.getSlotName().equals(STR_TASK_NAME_LIST))
                .collect(Collectors.toList());
        for (Slot taskNameListSlot: taskNameListSlotList) {
            String[] taskNameArray = taskNameListSlot.getNormedValue().trim().split("[\n、]");
            for (String taskName: taskNameArray) {
                taskName = taskName.trim();
                if (!StringUtils.isEmpty(taskName)) {
                    Slot slot = new Slot(STR_TASK_NAME_EQUAL, taskName, taskName, taskNameListSlot.getStartIndex(),
                            taskNameListSlot.getEndIndex(), taskNameListSlot.getScore(), true, taskNameListSlot.getSource(),
                            taskNameListSlot.getEvidence());
                    slotList.add(slot);
                }
            }
        }

        // 将IP拆出来、子账号拆出来
        List<Pair<String, String>> slotNamePairList = Lists.newArrayList(
                new Pair<>(STR_IP_LIST, STR_IP),
                new Pair<>(STR_BAIZE_IP_LIST, STR_BAIZE_IP),
                new Pair<>(STR_JIAFANG_IP_LIST, STR_JIAFANG_IP),
                new Pair<>(STR_ACCOUNT_INFO_LIST, STR_ACCOUNT_INFO),
                new Pair<>(STR_DATA_STATISTIC_OUTBOUND_TYPE_LIST, STR_DATA_STATISTIC_OUTBOUND_TYPE),
                new Pair<>(STR_CALLBACK_OUTBOUND_TYPE_LIST, STR_CALLBACK_OUTBOUND_TYPE),
                new Pair<>(STR_CALLBACK_STATUS_LIST, STR_CALLBACK_STATUS),
                new Pair<>(STR_OUTBOUND_CALLBACK_FIELD_LIST, STR_OUTBOUND_CALLBACK_FIELD),
                new Pair<>(STR_SMS_CALLBACK_FIELD_LIST, STR_SMS_CALLBACK_FIELD),
                new Pair<>(STR_INTENTION_CLASS_LIST, STR_INTENTION_CLASS),
                new Pair<>(STR_CALL_TEAM_NAME_LIST, STR_CALL_TEAM_NAME),
                new Pair<>(STR_SCRIPT_NAME_LIST, STR_SCRIPT_NAME),
                new Pair<>(STR_INTENT_LIST, STR_INTENT)
//                new Pair<>(STR_BAIZE_YUNYING_ACCOUNT_INFO_LIST, STR_BAIZE_YUNYING_ACCOUNT_INFO_LIST.replace("列表", "")),
//                new Pair<>(STR_JIAFANG_GUANLIYUAN_ACCOUNT_INFO_LIST, STR_JIAFANG_GUANLIYUAN_ACCOUNT_INFO_LIST.replace("列表", "")),
//                new Pair<>(STR_WAIBU_ZUOXIZUZHANG_ACCOUNT_INFO_LIST, STR_WAIBU_ZUOXIZUZHANG_ACCOUNT_INFO_LIST.replace("列表", "")),
//                new Pair<>(STR_WAIBU_ZUOXI_ACCOUNT_INFO_LIST, STR_WAIBU_ZUOXI_ACCOUNT_INFO_LIST.replace("列表", ""))
        );
        for (Pair<String, String> pair: slotNamePairList) {
            String listSlotName = pair.getKey();
            String slotName = pair.getValue();
            List<Slot> listSlotList = slotList.stream()
                    .filter(x -> x.getSlotName().equals(listSlotName))
                    .collect(Collectors.toList());
            for (Slot itemListSlot : listSlotList) {
                String[] itemArray = itemListSlot.getNormedValue().split("[|、]");
                for (String item : itemArray) {
                    item = item.trim();
                    if (!StringUtils.isEmpty(item)) {
                        Slot slot = new Slot(slotName, item, item, itemListSlot.getStartIndex(),
                                itemListSlot.getEndIndex(), itemListSlot.getScore(), true, itemListSlot.getSource(),
                                itemListSlot.getEvidence());
                        slotList.add(slot);
                    }
                }
                slotList.remove(itemListSlot);
            }
        }


        // 时间早于等于7点的，加12个小时
        List<Slot> expectedTimeSlotList = slotList.stream()
                .filter(x -> x.getSlotName().equals(STR_EXPECTED_START_TIME) || x.getSlotName().equals(STR_EXPECTED_END_TIME))
                .collect(Collectors.toList());
        for (Slot expectedTimeSlot: expectedTimeSlotList) {
            String normedValue = expectedTimeSlot.getNormedValue();
            if (normedValue.contains(":")) {
                String[] array = normedValue.split(":");
                if (array.length == 2) {
                    int hour = Integer.valueOf(array[0]);
                    if (hour <= 7) {
                        hour += 12;
                    }
                    normedValue = hour + ":" + array[1];
                    expectedTimeSlot.setNormedValue(normedValue);
                }
            }
        }

        return slotList;
    }

    private static boolean isOnlyContainSlots(List<Slot> slotList, Set<String> slotNameSet) {
        return slotList.stream().allMatch(slot -> slotNameSet.contains(slot.getSlotName()))
                && slotNameSet.size() == slotList.stream().map(slot -> slot.getSlotName()).distinct().count();
    }

    private static boolean isContainSlot(List<Slot> slotList, String slotName) {
        return slotList.stream().anyMatch(slot -> slotName.equals(slot.getSlotName()));
    }

    private static boolean isContainSlot(List<Slot> slotList, String slotName, String normedValue) {
        return slotList.stream().anyMatch(
                slot -> slotName.equals(slot.getSlotName()) && normedValue.equals(slot.getNormedValue()));
    }

    private static boolean isContainSlot(List<Slot> slotList, String slotName, int count) {
        return slotList.stream().filter(slot -> slotName.equals(slot.getSlotName())).count() == count;
    }

    private static long getSlotCount(List<Slot> slotList, String slotName) {
        return slotList.stream().filter(slot -> slotName.equals(slot.getSlotName())).count();
    }

    private static List<List<Slot>> getSlotGroupList(List<Slot> slots, QueryInfo queryInfo) {
        // 生成各行的起始位置列表
        List<Integer> lineStarts = new ArrayList<>();
        lineStarts.add(0);
        int idx = 0;
        for (char c: queryInfo.getRawQuery().toCharArray()) {
            if (c == '\n') {
                lineStarts.add(idx + 1);
            }
            idx += 1;
        }

        // 转换为数组以便二分查找
        int[] lineStartsArray = lineStarts.stream().mapToInt(Integer::intValue).toArray();

        // 初始化分组列表
        List<List<Slot>> groups = new ArrayList<>();
        for (int i = 0; i < lineStarts.size(); i++) {
            groups.add(new ArrayList<>());
        }

        // 分配每个slot到对应的组
        for (Slot slot : slots) {
            int start = slot.getStartIndex();
            // 使用二分查找确定行索引
            int lineIndex = Arrays.binarySearch(lineStartsArray, start);
            if (lineIndex < 0) {
                lineIndex = -lineIndex - 2;
            }
            if (lineIndex >= 0 && lineIndex < groups.size()) {
                groups.get(lineIndex).add(slot);
            }
        }

        return groups;
    }

    /**
     * 本行有线路slot，其它行也有线路slot，则抽取出本行的指定slotName的slot
     *
     * @param lineSlot
     * @param slotName
     * @param slotGroupList
     * @return
     */
    private static List<Slot> getExclusiveSlotsForLineSlot(Slot lineSlot, String slotName, List<List<Slot>> slotGroupList) {
        List<Slot> slotGroup = null;
        boolean hasOtherLineSlot = false;
        for (List<Slot> slotList: slotGroupList) {
            if (slotList.contains(lineSlot)) {
                slotGroup = slotList;
            } else {
                if (slotList.stream().anyMatch(slot -> lineSlot.getSlotName().equals(slot.getSlotName()))) {
                    hasOtherLineSlot = true;
                }
            }
        }
        if (hasOtherLineSlot) {
            return slotGroup.stream().filter(slot -> slotName.equals(slot.getSlotName())).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private static List<AccountProductInfo> getAccountProductInfoList(
            Set<String> requiredAccountSet, List<Pair<String, String>> productAccountSuffixPairList, List<Pair<String, String>> productModePairList) {
        Set<String> accountSet = new HashSet<>(requiredAccountSet);
        // 将 productAccountSuffixPairList 中的内容转化为 account
        for (Pair<String, String> pair: productAccountSuffixPairList) {
            String product = pair.getKey();
            String accountSuffix = pair.getValue();
            String accountPrefix = ProductAccountTool.getInstance().getAccountPrefix(product);
            if (accountPrefix != null) {
                String account = accountPrefix + accountSuffix;
                accountSet.add(account);
            }
        }

        List<AccountProductInfo> infoList = new ArrayList<>();
        // 处理 productModePairList
        for (Pair<String, String> pair: productModePairList) {
            String product = pair.getKey();
            String entranceMode = pair.getValue();
            String account = ProductAccountTool.getInstance().getAccount(product, entranceMode);
            if (account != null) {
                AccountProductInfo info = new AccountProductInfo(account, product, entranceMode);
                infoList.add(info);

                // 和 account 重复的，从 accountSet 中移出
                if (accountSet.contains(account)) {
                    accountSet.remove(account);
                }
            } else if (product != null || entranceMode != null) {
                // 获取不到 account 时
                String accountPrefix = ProductAccountTool.getInstance().getAccountPrefix(product);
                boolean isInAccountSet = false;
                if (accountPrefix != null) {
                    for (String tmpAccount : accountSet) {
                        if (tmpAccount.startsWith(accountPrefix)) {
                            isInAccountSet = true;
                        }
                    }
                }
                if (!isInAccountSet) {
                    AccountProductInfo info = new AccountProductInfo(account, product, entranceMode);
                    infoList.add(info);
                }
            }
        }
        // 处理 accountSet
        for (String account: accountSet) {
            String product = ProductAccountTool.getInstance().getProduct(account);
            String entranceMode = ProductAccountTool.getInstance().getEntranceMode(account);
            AccountProductInfo info = new AccountProductInfo(account, product, entranceMode);
            infoList.add(info);
        }
        // 如果 infoList 为空，那要生成一个全未知的
        if (infoList.isEmpty()) {
            AccountProductInfo info = new AccountProductInfo(null, null, null);
            infoList.add(info);
        }
        return infoList;
    }

    private static String getStrDate(String year, String month, String day) {
        Date date = new Date();
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(year)) {
            sb.append(year);
        } else {
            sb.append(DatetimeUtils.getYear(date));
        }
        sb.append("-");
        if (!StringUtils.isEmpty(month)) {
            sb.append(String.format("%02d", Integer.parseInt(month)));
        } else {
            sb.append(String.format("%02d", DatetimeUtils.getMonth(date)));
        }
        sb.append("-");
        if (!StringUtils.isEmpty(day)) {
            sb.append(String.format("%02d", Integer.parseInt(day)));
        } else {
            sb.append(String.format("%02d", DatetimeUtils.getDayOfMonth(date)));
        }
        return sb.toString();
    }

    private static Pair<String, String> getTaskCreateTimeBoundPair(Slot slot) {
        if (STR_MORNING.equals(slot.getNormedValue())) {
            return new Pair<>("08:00", "12:00");
        } else if (STR_AFTERNOON.equals(slot.getNormedValue())) {
            return new Pair<>("12:00", "19:00");
        }
        return null;
    }

    public static List<AbstractInstructionBean> getInstructionBeanList(
            String domain, String rawQuery, ChatGroup chatGroup, String creator) {
        String query = Preprocessor.getNormalizedQuery(rawQuery);
        QueryInfo queryInfo = new QueryInfo(domain, query);
        List<IntentInfo> intentInfoList = IntentRuleDetect.predict(queryInfo);
        if (!CollectionUtils.isEmpty(intentInfoList)) {
            List<Slot> slotList = SlotEngine.getSlotList(queryInfo);
            intentInfoList = IntentPostprocessor.postprocess(intentInfoList, slotList);
            List<AbstractInstructionBean> instructionBeanList = InstructionEngine.getInstructionBeanList(chatGroup, creator, queryInfo, intentInfoList, slotList);
            return instructionBeanList;
        } else {
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        ChatGroup chatGroup = ChatGroup.CHAT_TEST;
        String creator = "WangQi";
        String domain = Domain.SHANGHUYUNYING;
//        String query = "好分期已推，3条线路都用，并发白泽加到1000,仙人500，得心6k";
//        String query = "360 661提了3个任务，分别使用3条线呼叫";
//        String query = "首轮呼，用仙人线路";
//        String query = "仙人线路并发开1000，白泽线路并发开500";
//        String query = "电信屏蔽泉州";
//        String query = "数据已推，白泽线路 11:00前呼完";
//        String query = "36  开测，三条线路，小并发";
//        String query = "还呗上午提交的任务（不含线路测试任务）\n" +
//                "10：35——11：50，第一轮，得心线路\n" +
//                "13：30——14：30，第二轮，仙人线路";
//        String query = "公众号：\n" +
//                "guomei8888任务已上传\n" +
//                "10:00-12:00   得心\n" +
//                "13:30-15:00  仙人";
//        String query = "guomei6666已上传，策略如下\n" +
//                "上午 10:00-11:40  得心-仙人 （早间任务不加入补呼）\n" +
//                "下午 13:30~16:00  白泽-仙人 （下午开始只跑01、chip01任务）";
//        String query = "①还呗线路对比任务（3个）沿用对应的线路呼第三轮\n" +
//                "②还呗上午其它任务（不含线路对比），14：10-14：40，用仙人线路呼第三轮\n" +
//                "③还呗下午提交的任务，14：15-15：00，用得心线路呼第一轮；15：15-16：00，用仙人线路呼第二轮；";
//        String query = "@王一   xinyongfei666的数据已上传（今日第一批数据84w）\n" +
//                "第一轮 09:35 -10.15   仙人\n" +
//                "第二轮 10.20 -11.05   移动任务 1 2 3，联通任务，电信任务，话术任务 白泽；   其他几个任务 得心呼叫\n" +
//                "稍后还有第二批";
//        String query = "联通屏蔽地区广州帮忙放开一下";
//        String query = "吉用花8888，提了几个任务，\n" +
//                "小并发，麻烦用仙人线呼一下，呼通到1万就暂停";
//        String query = "襄阳、长春、杭州、西安  加入银闪花挂短、银闪花公众号的屏蔽";
//        String query = "盲区：北京、新疆、西藏";
//        String query = "不含苏州的任务";
//        String query = "工薪花12：00停，下午两点在开始";
//        String query = "电信全开";
//        String query = "长沙、西安  加入银闪花挂短、银闪花公众号的屏蔽";
//        String query = "薇钱包888 已推  1876862  移动电信的用户限时002线路1k并发，联通的用仙人的008和076各开100并发";
//        String query = "消息ID：\n" +
//                "操作：开始任务\n" +
//                "账号：\n" +
//                "业务：薇钱包\n" +
//                "仙人008线路 100并发 包含移动、电信、联通任务 \n" +
//                "仙人076线路 100并发 包含移动、电信、联通任务 \n" +
//                "限时线路 1000并发 包含移动、电信、联通任务";
//        String query = "长沙  加入银闪花挂短、银闪花公众号的屏蔽";
//        String query = "拍拍 极融促活原线路补呼";
//        String query = "吉用花公众号提交的用限时1000并发下发哈";
//        String query = "重庆、长春、长沙 加入银闪花挂短、银闪花公众号的屏蔽";
//        String query = "10 38任务名带苹果的 相同线路和并发打开下";
//        String query = "吉用花公众号提交的继续下发哈";
//        String query = "麻烦用限时jr303呼一下100并发";
//        String query = "盈点公众号，提了任务，麻烦用限时jr303呼一下100并发。呼完再用仙人线呼一遍";
//        String query = "用户仙人008,1000并发";
//        String query = "jietiao366666 总量245537 帮忙全部用jr032线路 并发控制上限150";
//        String query = "盈点公众号，提了任务，麻烦用限时jr303呼一下，200并发。";
//        String query = "众安贷公众号得心主叫1203开3000并发";
//        String query = "下午三点开始众安挂短那的034降3000并发 公众号下午的任务各开3000并发";
//        String query = "呼通5000暂停";
//        String query = "吉用花6616提交的用jr013继续下发， 呼通5000暂停";
//        String query = "海花公众号推过去一批数，帮忙使用得心hhqb056下发，总共1500并发";
//        String query = "公众号呼一下";
//        String query = "huaya888呼一下";
//        String query = "花鸭888呼一下";
//        String query = "花鸭公众号呼一下";
//        String query = "吉用花6616呼完之后这些任务用仙人线在呼一遍哈，4点前结束任务";
//        String query = "吉用花888提交的用限时jr013开始外呼，500并发";
//        String query = "吉用花公众号和挂短提交的用限时1毛1的线，各500下发哈";
//        String query = "国美666账号已提交 9:00-12:00    限时线路   300 并发";
//        String query = "9:00-12:00    限时线路  1 轮 300 并发";
//        String query = "haohuijie888  任务已提交 10点开始\n" +
//                "用 仙人线058    跑1轮 500并发";
//        String query = "分期乐包含xr标记的任务全开，走仙人053，并发2K";
//        String query = "分期乐包含xr的任务全开，走仙人053，并发2K";
//        String query = "唐山  加入花鸭888的屏蔽";
//        String query = "唐山  加入银闪花挂短YSH666、银闪花公众号YSH的屏蔽";
//        String query = "吉用花888并发调整到500把";
//        String query = "分期乐公众号使用得心线路，并发上限4k，10点开始，不设置复呼";
//        String query = "操作：开始任务\n" +
//                "账号：fenqile888\n" +
//                "业务：分期乐\n" +
//                "仙人线路 2000并发  包含xr的任务";
//        String query = "操作：开始任务\n" +
//                "账号：langshun05\n" +
//                "业务：极闪融\n" +
//                "9:00开始 限时ls001线路 100并发";
//        String query = "guomei6666  任务已提交\n" +
//                "9:00-12:00    限时线路  1 轮 300 并发";
//        String query = "极闪融数据已经推送过去了哈，9点就可以启动呼叫了";
//        String query = "盈点公众号，并发开到500";
//        String query = "吉用花661刚刚暂停了的移动任务重新开一下用限时102,450并发";
//        String query = "海花挂短屏蔽地区：北京新疆西藏、上海";
//        String query = "执行指令ID：9fe6960eeb64409199325d15466aeccc";
//        String query = "吉用花公众好提交的用限时1毛3的1000并发，9点下发哈";
//        String query = "用得心线路开呼，任务：【\n" +
//                "aaa\n" +
//                "bbb\n" +
//                "\n】";
//        String query = "chen001 账号屏蔽上海、保定、河南";
//        String query = "chen001 账号取消屏蔽上海、保定、河南";
//        String query = "太原，广州加入chen001的屏蔽，任务【5327789】";
//        String query = "chen001 仙人线路 50并发 14:00开始呼叫";
//        String query = "chen001 仙人线路 50并发 14:00重启呼叫";
//        String query = "chen001 仙人线路 50并发 14:00复呼";
//        String query = "chen001 仙人线路 50并发开呼";
//        String query = "chen001 仙人线路 50并发复呼";
//        String query = "chen001 仙人线路 12:04 停止呼叫";
//        String query = "chen001 并发改到70";
//        String query = "chen001 并发改到70 15:05开始";
//        String query = "chen001 13:15 停止任务";
//        String query = "苏州加入YSH的屏蔽";
//        String query = "删除指令ID：c0c8e341e2c946d0a5259cb985152053";
//        String query = "银闪花公众号新增一批数据，使用ysh0707线路启动，1000并发";
//        String query = "操作：开始任务\n" +
//                "账号：YSH\n" +
//                "业务：银闪花公众号\n" +
//                "任务状态：待执行\n" +
//                "ysh0707线路 1000并发 ";
//        String query = "工薪花仙人jr001、jr002停止外呼";
//        String query = "分期乐，得心FQL707线路并发调整到650";
//        String query = "分期乐，仙人jrdt007线路的任务改用得心FQL707线路650并发";
//        String query = "weiqianbao888，仙人076并发调整到700";
//        String query = "删除指令：327463c4f90e459a95217d109e13bebb";
//        String query = "账号：chen001，启动任务，仙人线路，10并发";
//        String query = "baotai10重启任务，用jrdt002线路 ,500并发";
//        String query = "海花的挂短，帮忙屏蔽下广州、上海、宁波、佛山";
//        String query = "haihua666，帮忙屏蔽下广州、上海、宁波、佛山、北京西藏新疆";
//        String query = "chen001，集线比调为30k";
//        String query = "任务名含补3标记的并发3000开呼";
//        String query = "任务名含{补3}并发3000开呼";
//        String query = "盈点公众号认证线路开呼";
//        String query = "chen001调整并发到200";
//        String query = "weiqianbao333，任务名包含{白泽1}和{白泽2}暂停";
//        String query = "weiqianbao333，baotai27 暂停";
//        String query = "baotaitestai 重启外呼" +
//                "任务名包含baize1标记 白泽线路 10并发\n" +
//                "任务名包含{baize2} 白泽线路 20并发";
//        String query = "baotaitestai  重启包含{模板2}和{模板3}的任务，使用白泽线路 10并发";
//        String query = "baotaitestai  重启【模板2】和【模板3】的任务，使用白泽线路 10并发";
//        String query = "baotaitestai  重启任务【模板2】和【模板3】，使用白泽线路 10并发";
//        String query = "weiqianbao333，重启包含“xs”的任务名，用限时202线路800并发";
//        String query = "海花公众号，武汉、石家庄取消屏蔽";
//        String query = "13501234567 2025-7-8 13:46:53 下载录音";
//        String query = "下载录音仙人录音\n" +
//                "13501234567 2025-27-28 13:46:53\n" +
//                "13501234566 2025-6-8 13:45:50\n";
//        String query = "18848510723 07-23 下载录音";
//        String query = "下载录音\n" +
//                "18848510723 2025-07-23\n" +
//                "18229006506 2025.7.17";
//        String query = "随州、咸宁、仙桃、黄石、武汉、石家庄加入屏蔽海花公众号屏蔽";
//        String query = "海花公众号：南宁加入屏蔽";
//        String query = "chen001暂停任务【576414-20250729移动-白泽平安\n" +
//                "578055-20250729联通-白泽平安\n" +
//                "644370-20250729联通-白泽平安\n" +
//                "634803-20250729移动-白泽平安\n" +
//                "640814-20250729移动-白泽平安\n" +
//                "577487-20250729联通-白泽平安\n" +
//                "578118-20250729移动-白泽平安\n" +
//                "645383-20250729联通-白泽平安\n" +
//                "640811-20250729移动-白泽平安\n" +
//                "576547-20250729联通-白泽平安\n" +
//                "634808-20250729移动-白泽平安\n" +
//                "645403-20250729联通-白泽平安\n" +
//                "577535-20250729联通-白泽平安\n" +
//                "578109-20250729移动-白泽平安\n" +
//                "644368-20250729联通-白泽平安\n" +
//                "640813-20250729移动-白泽平安\n" +
//                "645384-20250729联通-白泽平安\n" +
//                "578235-20250729联通-白泽平安\n" +
//                "622762-20250729移动-白泽平安\n" +
//                "576637-20250729联通-白泽平安\n" +
//                "576365-20250729联通-白泽平安\n" +
//                "578090-20250729移动-白泽平安\n" +
//                "632749-20250729联通-白泽平安\n" +
//                "576356-20250729联通-白泽平安\n" +
//                "576364-20250729联通-白泽平安\n" +
//                "632743-20250729移动-白泽平安\n" +
//                "634805-20250729移动-白泽平安\n" +
//                "578244-20250729联通-白泽平安\n" +
//                "620425-20250729联通-白泽平安\n" +
//                "578254-20250729联通-白泽平安\n" +
//                "578107-20250729联通-白泽平安\n" +
//                "578160-20250729移动-白泽平安\n" +
//                "578057-20250729移动-白泽平安\n" +
//                "578080-20250729联通-白泽平安\n" +
//                "578124-20250729联通-白泽平安\n" +
//                "578136-20250729移动-白泽平安\n" +
//                "578101-20250729联通-白泽平安\n" +
//                "634806-20250729联通-白泽平安\n" +
//                "645399-20250729联通-白泽平安\n" +
//                "576672-20250729移动-白泽平安\n" +
//                "576568-20250729联通-白泽平安\n" +
//                "578128-20250729联通-白泽平安\n" +
//                "576387-20250729移动-白泽平安\n" +
//                "625183-20250729移动-白泽平安\n" +
//                "577520-20250729移动-白泽平安\n" +
//                "576608-20250729移动-白泽平安\n" +
//                "622739-20250729联通-白泽平安】";
//        String query = "15996415981   6/19日营销的  录音麻烦给下";
//        String query = "下载录音\n" +
//                "15996415981   6.19\n" +
//                "15996415980   6/10";
//        String query = "海花公众号针对限时zy002线路的任务暂停外呼";
//        String query = "吉用花提交的用标注的线路呼哈，仙人限时并发都先开500，9点开始哈";
//        String query = "吉用花提交的用标注的线路呼哈，仙人限时得心并发都先开500，9点开始哈";
//        String query = "吉用花提交的用标注的线路呼哈，仙人jrsan001、限时jr002、得心001并发都先开500，9点开始哈";
//        String query = "吉用花提交的用标注的线路呼哈，\n" +
//                "仙人限时并发都先开500，\n" +
//                "得心并发开1000，\n" +
//                "9点开始哈";
//        String query = "操作类型：新开主账号\n" +
//                "商户名称：test0717\n" +
//                "主账号：baotai59\n" +
//                "主账号密码：wangqi123@\n" +
//                "联系人：【贷款】银闪花公众号\n" +
//                "是否加密线路：False\n" +
//                "产品名称：59众安保险\n" +
//                "二级行业：保险\n" +
//                "白泽ip：192.168.106.208|192.168.106.201|192.168.23.43\n" +
//                "甲方ip：192.168.106.207|192.168.106.200|192.168.23.43\n" +
//                "白泽运营账号：baotai59sl/shilin123/石林|baotai59wy/wangyi123/王一|baotai59zjw/zhangjiawei/张家唯\n" +
//                "甲方管理员账号：baotai59888/wangqi123@/甲方管理员\n";
//        String query = "操作类型：新开子账号\n" +
//                "主账号：baotai61\n" +
//                "外部坐席组长账号：baotai61zxzz/baotai61zxzz/坐席组长\n" +
//                "外部坐席账号：baotai61zx1/baotai61zx1/外部坐席1|baotai61zx2/baotai61zx2/外部坐席2\n";
//        String query = "操作类型：添加角色\n" +
//                "主账号：baotai59   \n" +
//                "角色名：外部坐席（白泽）   \n" +
//                "复用角色：外部坐席   \n" +
//                "IP：192.168.106.207|192.168.106.200|192.168.23.43\n";
//        String query = "baotai10账号已入，量级299588，配置仙人 并发先按500   9点全启";
//        String query = "haofenqi888，切换到限时281线路，50并发";
//        String query = "操作类型：新开子账号\n" +
//                "主账号：baotai59\n" +
//                "外部坐席组长（白泽）账号：baotai59zxzz/baotai59zxzz/坐席组长\n" +
//                "外部坐席（白泽）账号：baotai59zx1/baotai59zx1/外部坐席1|baotai59zx2/baotai59zx2/外部坐席2\n";
//        String query = "操作类型：添加角色IP\n" +
//                "主账号：baotai59\n" +
//                "角色名：外部坐席（白泽）   \n" +
//                "IP：192.168.106.201|192.168.23.42\n";
//        String query = "yingdian888,043线路并发调整到3000";
//        String query = "chen001 仙人线路原并发重启任务";
//        String query = "chen001 原并发重启任务";
//        String query = "chen001全天屏蔽天津、福建、石家庄";
//        String query = "chen001用仙人线路2并发开呼";
//        String query = "chen001停止呼叫";
//        String query = "海花公众号：西安加入全天屏蔽";
//        String query = "langshun05：宁波 沈阳 保定  长沙屏蔽";
//        String query = "梧州 石家庄 兰州 河池 崇左 宜昌 取消langshun05屏蔽";
//        String query = "海花公众号，广州放开全天屏蔽";
//        String query = "chen001 追加任务到仙人线路";
//        String query = "海花公众号并发调整到300";
//        String query = "开启带有AA、BB标记的任务开呼";
//        String query = "任务名称带有甲甲、乙乙、丙丙标记的开呼";
//        String query = "白泽线路 10并发 以{-2}结尾的任务 开呼";
//        String query = "chen001 调整并发 限时ls001线路的任务 改用限时ls001线路 1500并发";
//        String query = "chen001 操作：调整并发 jrdt001线路的任务 改用限时dt001线路 20并发";
//        String query = "chen001 改为30并发";
//        String query = "weiqianbao661，仙人008线路并发调整到3000";
//        String query = "{\n" +
//                "   \"操作类型\": \"编辑商户线路\",\n" +
//                "   \"操作内容\": [\n" +
//                "      {\n" +
//                "         \"所属商户\": \"chen001\",\n" +
//                "         \"商户线路名称\": \"【保险】chen001测试1\",\n" +
//                "         \"商户线路组成\": [\n" +
//                "            {\n" +
//                "               \"运营商\": \"联通\",\n" +
//                "               \"分省线路列表\": [\n" +
//                "                  {\n" +
//                "                     \"省份列表\": [\n" +
//                "                        \"河南\"\n" +
//                "                     ],\n" +
//                "                     \"线路列表\": [\n" +
//                "                        \"第二条供应商线路111111\",\n" +
//                "                        \"第二条供应商线路\",\n" +
//                "                        \"供应商04-AI线路001-等待时长10s接通\"\n" +
//                "                     ]\n" +
//                "                  },\n" +
//                "                  {\n" +
//                "                     \"省份列表\": [\n" +
//                "                        \"湖南\"\n" +
//                "                     ],\n" +
//                "                     \"线路列表\": [\n" +
//                "                        \"从成都市\",\n" +
//                "                        \"供应线路test\"\n" +
//                "                     ]\n" +
//                "                  }\n" +
//                "               ]\n" +
//                "            }\n" +
//                "         ]\n" +
//                "      },\n" +
//                "      {\n" +
//                "         \"所属商户\": \"chen001\",\n" +
//                "         \"商户线路名称\": \"【保险】chen001测试2\",\n" +
//                "         \"商户线路组成\": [\n" +
//                "            {\n" +
//                "               \"运营商\": \"全部\",\n" +
//                "               \"分省线路列表\": [\n" +
//                "                  {\n" +
//                "                     \"省份列表\": [\n" +
//                "                        \"河南\"\n" +
//                "                     ],\n" +
//                "                     \"线路列表\": [\n" +
//                "                        \"供应商04-AI线路001-等待时长10s接通\"\n" +
//                "                     ]\n" +
//                "                  },\n" +
//                "                  {\n" +
//                "                     \"省份列表\": [\n" +
//                "                        \"湖南\"\n" +
//                "                     ],\n" +
//                "                     \"线路列表\": [\n" +
//                "                        \"从成都市\",\n" +
//                "                        \"第二条供应商线路111111\"\n" +
//                "                     ]\n" +
//                "                  }\n" +
//                "               ]\n" +
//                "            }\n" +
//                "         ]\n" +
//                "      }\n" +
//                "   ]\n" +
//                "}";
//        String query = "YSH追加任务，用得心ysh0716线路，900并发";
//        String query = "chen001包含{五六}、{七八}任务用仙人线路10并发复呼";
//        String query = "查看全天屏蔽";
//        String query = "吉用花公众号任务对比3-限时-10点\n吉用花公众号任 务对比5-限时-10点\n吉用花公众号任务对比1-限时-10点\n吉用花公众号任务对比11-限时-10点\n吉用花公众号任务对比9-限时-10点\n 吉用花公众号任务对比7-限时-10点";
//        String query = "操作类型：配置回调地址\n" +
//                "主账号：baotai59\n" +
//                "查询范围：AI外呼、人机协同、人工直呼\n" +
//                "任务回调地址：http://192.168.231.112:4006/outbound\n" +
//                "通话回调范围：AI外呼、人机协同、人工直呼\n" +
//                "回调状态：呼叫成功、未接通、商户黑名单\n" +
//                "回调字段：手机号、姓名、公司、备注\n" +
//                "快速回调接口：http://192.168.231.112\n" +
//                "话后回调接口（新）：http://192.168.231.112\n" +
//                "文本补推接口：http://192.168.231.112\n" +
//                "M短信接口：http://192.168.231.112\n" +
//                "通话回调接口（旧）：http://192.168.231.112\n" +
//                "短信回调字段：手机号、姓名、公司、备注\n" +
//                "短信回调地址：http://192.168.231.112\n" +
//                "上行短信回调地址：http://192.168.231.112\n" +
//                "IP配置：192.168.0.1|192.168.0.2\n";
//        String query = "操作类型：编辑供应线路\n" +
//                "针对线路：仙人001\n" +
//                "并发：20";
//        String query = "海花公众号推过去一批数，以{_1}结尾，使用hhqb0707投放，400并发";
//        String query = "包含{官方}、{cc}的任务用限时994线路1000并发\n" +
//                "包含{aa}的任务用限时993线路900并发\n" +
//                "包含{bb}的任务用仙人002线路800并发";
//        String query = "操作：查看任务模板\n" +
//                "模板编号：343";
//        String query = "盈点公众号开始任务\n" +
//                "以{_1}结尾的用ysh0716线路启动700并发\n" +
//                "以{_2}结尾的用YSHBZ线路启动500并发";
//        String query = "chen001调整并发\n" +
//                "限时993线路调整到1000并发\n" +
//                "限时994线路调整到1100并发\n" +
//                "限时008线路调整到1200并发\n" +
//                "仙人002线路调整到1300并发 \n" +
//                "得心716线路调整到1400并发";
//        String query = "操作类型：获取任务数据\n" +
//                "主账号：chen001\n";
//        String query = "下载音频 \n" +
//                "13657062816 2025-9-4 14:01:17\n" +
//                "18696908420 2025-9-6 11:06:34";
//        String query = "taoyirong999 仙人055线路的任务切换为限时线路";
//        String query = "taoyirong999仙人055线路切换为限时线路";
//        String query = "taoyirong999仙人055线路切换为限时065线路";
//        String query = "操作类型：任务数据汇报\n" +
//                "shuxunda01 2025-09-09\n" +
//                "意向分类：A、B、C、D、E、F、G、其他\n" +
//                "输出平均通时：是\n" +
//                "输出总通时：是";
//        String query = "langshun06：上海 天津 成都 贵阳 长沙 西安 深圳 济宁 沈阳 济南 百色 潍坊 加入全天屏蔽";
//        String query = "shuxunda01追加任务";
//        String query = "langshun06屏蔽：上海、天津、成都、贵阳、长沙、西安、深圳、济宁、沈阳、济南、百色、潍坊";
//        String query = "操作类型：创建任务模板\n" +
//                "主账号：chen001\n" +
//                "模板名称：测试新建模板0910\n" +
//                "任务类型：AI外呼\n" +
//                "隔日续呼：否\n" +
//                "执行话术：最新外呼任务话术\n" +
//                "拨打时段：9:00-12:00、13:00-18:00\n" +
//                "自动补呼：关\n";
//        String query = "操作类型：创建任务模板\n" +
//                "主账号：chen001\n" +
//                "模板名称：人机协同测试新建模板0910\n" +
//                "任务类型：人机协同\n" +
//                "隔日续呼：否\n" +
//                "执行话术：最新外呼任务话术\n" +
//                "坐席组：坐席组007\n" +
//                "处理方式：监听\n" +
//                "集线比：10\n" +
//                "占用等级：高\n" +
//                "虚拟坐席系数：0.2\n" +
//                "拨打时段：9:00-12:00、13:00-18:00\n" +
//                "自动补呼：关\n";
//        String query = "操作类型：创建任务模板\n" +
//                "主账号：chen001\n" +
//                "模板名称：人机协同测试新建模板0911\n" +
//                "任务类型：人机协同\n" +
//                "执行话术：test00233\n" +
//                "坐席组：坐席组007\n";
//        String query = "taoyirong999开始任务\n" +
//                "带{限时}的任务用限时线路3000并发\n" +
//                "不带{限时}的任务用仙人线路2000并发";
//        String query = "taoyirong999开始任务\n" +
//                "带{限时}的任务用限时003线路3000并发\n" +
//                "不带{限时}的任务用仙人002线路2000并发";
//        String query = "19943972111 2025/9/7 09:25:26\n" +
//                "下载录音";
//        String query = "下载录音\n"
//                + "19943972111 2025/9/7 09:25:26\n"
//                + "19943971222 2025/9/8 08:15:16\n"
//                + "19943971221 7月6号 10:11:16\n";
//        String query = "19943972111 2025/9/7 下载录音";
//        String query = "百色、北海、崇左、防城港、贵港、河池、来宾、南阳、钦州、潍坊、长沙、常州、福州、广州、金华、兰州、泉州、温州、东营、嘉兴、无锡、德州、哈尔滨、汉中、菏泽、衡水、惠州、克拉玛依、连云港、临沂、六安、马鞍山、南昌、秦皇岛、商丘、石家庄、泰安、天水、威海、咸阳、烟台、枣庄、张家口、长治加入chen001的全天屏蔽";
//        String query = "操作类型：任务数据汇报\n" +
//                "主账号：zhongandai999\n" +
//                "输出平均通时：是\n" +
//                "输出总通时：是";
//        String query = "带{619834}、{619833}、{619836}结尾的任务暂停";
//        String query = "chen001开始任务 包含{xianshi}的任务用xianshi线路1000并发";
//        String query = "chen001 复呼任务 仙人001线路 20并发 任务名称：【测试001\n测试002】";
//        String query = "chen001调整并发\n" +
//                "限时012线路调整为7500并发\n" +
//                "仙人055线路调整为500并发";
//        String query = "chen001调整并发\n" +
//                "限时012线路调整为7500并发\n" +
//                "仙人线路调整为500并发";
//        String query = "chen001调整并发\n" +
//                "限时线路调整为7500并发\n" +
//                "仙人线路调整为500并发";
//        String query = "chen001调整并发\n" +
//                "限时008线路调整到1000并发\n" +
//                "得心716线路调整到900并发\n" +
//                "仙人010线路调整到800并发\n" +
//                "限时993线路调整到700并发 \n" +
//                "得心916线路调整到600并发\n" +
//                "仙人011线路调整到500并发\n" +
//                "仙人008线路调整到400并发";
//        String query = "操作类型：配置回调地址\n" +
//                "主账号：huayouqian888  \n" +
//                "查询范围：AI外呼、人机协同、人工直呼\n" +
//                "通话回调范围：AI外呼、人机协同\n" +
//                "回调状态：呼叫成功、未接通、商户黑名单\n" +
//                "回调字段：手机号\n" +
//                "通话回调接口(旧)：http://rxy.toutbound.status.ruixiaoyun.net/toutbound-call-status-push/statusPush/outbound/baotai/task/10109\n" +
//                "M短信接口、快速回调接口：\n" +
//                "http://rxy.toutbound.status.ruixiaoyun.net/toutbound-call-status-push/statusPush/outbound/baize/task/10109\n" +
//                "话后回调接口（新）、文本补推接口：http://rxy.toutbound.status.ruixiaoyun.net/toutbound-call-status-push/statusPush/outbound/baize/task/10109";
//        String query = "银闪花公众号开始：以{_1}结尾的任务用SBYXJRZ线路1000并发，";
//        String query = "baotai27并发调整为2500\n" +
//                "针对线路：（主叫jrdt007）（转人工）";
//        String query = "海花公众号开始外呼\n" +
//                "以{_test1}结尾、8点到9点之间上传的任务，使用jrzy002线路1000并发\n" +
//                "以{_test2}结尾、上午创建的任务，使用限时zy001线路1000并发";
//        String query = "chen001 包含{测试}、16:58到17:02之间上传的任务 使用仙人线路10并发";
//        String query = "查看通话记录 2025-10-21到2025-10-22 文本包含“对你来了一点了”";
//        String query = "查看通话记录 2025-10-21 文本包含“对你来了一点了，是” 得心线路";
//        String query = "查看通话记录 2025-10-21 意图包含:异常_高度疑似假通、异常_疑似假通 得心线路";
//        String query = "查看通话记录 2025-10-27 意图包含：异常_高度疑似假通、异常_疑似假通";
//        String query = "操作类型：任务话术数据汇报\n" +
//                " 主账号：baotai10 \n" +
//                "任务名称：测试任务1、测试任务2";
//        String query = "yunshenghua888：长沙 重庆放开全天屏蔽";
        String query = "yunshenghua888：长沙 重庆放开\n" +
                "全天屏蔽：否";
//        String query = "展示账号锁定并发";
//        String query = "chen001调整并发\n" +
//                "仙人078线路调整为4500并发\n" +
////                "得心1113线路调整为6500并发";
//        String query = "操作类型：新开主账号\n" +
//                "商户名称：test0717\n" +
//                "主账号：baotai82\n" +
//                "联系人：baotai82\n" +
//                "产品名称：82众安保险\n" +
//                "二级行业：保险\n" +
//                "甲方管理员账号：baotai82888/wangqi123@/甲方管理员\n";
//        String query = "操作类型：替换语料名称和内容\n" +
//                "话术名称：test0029BBA1B0F4D38\n" +
//                "原文本：泰康\n" +
//                "新文本：众安";
//        String query = "操作类型：停用话术\n" +
//                "话术名称：保险测试话术\n";
//        String query = "操作类型：线路话术报备\n" +
//                "话术名称：保险测试话术、LS钱小友小程序-钱小友AI拉新-C1.0yy-minimax\n";
//        String query = "18790511031，15088150605 13475650926，2026-01-11，调取录音";
//        String query = "下载录音\n" +
//                "18790511031 2026-01-11 \n" +
//                "15088150605 2026-01-12 \n" +
//                "13475650926 2026-01-13 ";
//        String query =
//                "huoshan02调整并发\n" +
//                "2026011202线路调整为600并发，\n" +
//                "BXYCRZHS1线路调整为600并发，\n" +
//                "01010线路调整为300并发";
//        String query = "线路包含“【贷款】数迅达好周转限时指定线路（主叫sxd001）线路” 并发800 开始任务";
//        String query = "操作类型：恢复任务\n" +
//                "主账号：chen001\n";

        Set<Keyword> keywordSet = BaizeClientFactory.account2password.keySet().stream()
                .map(account -> new Keyword(STR_ACCOUNT, account.toLowerCase(), account.toLowerCase(), 1.0f, "custom_" + domain))
                .collect(Collectors.toSet());
        Set<String> domainSet = Sets.newHashSet(domain);
        AbstractSlotRule whitelistSLotRule = new WhitelistSlotRule(keywordSet, domainSet);
        SlotRuleDetect.resetCustomSlotRuleList(domain, Lists.newArrayList(whitelistSLotRule));

        List<AbstractInstructionBean> instructionBeanList = InstructionEngine.getInstructionBeanList(domain, query, chatGroup, creator);
        System.out.println(instructionBeanList.size());
        instructionBeanList.forEach(instructionBean -> {
            System.out.println(instructionBean.toDescription());
//            ExecuteInstructionUtils.addUnexecutedInstructionBean(instructionBean);
//            ExecuteInstructionUtils.executeInstructionBean("", instructionBean.getInstructionId());
        });
        System.out.println(JsonUtils.toJson(instructionBeanList, true));
    }
}
