package org.example.chat;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.MsgRecord;
import org.example.chat.bean.MsgType;
import org.example.chat.utils.*;
import org.example.instruction.InstructionEngine;
import org.example.instruction.bean.*;
import org.example.ruledetect.SlotRuleDetect;
import org.example.ruledetect.bean.Domain;
import org.example.ruledetect.bean.Keyword;
import org.example.ruledetect.slotrule.AbstractSlotRule;
import org.example.ruledetect.slotrule.WhitelistSlotRule;
import org.example.utils.CollectionUtils;
import org.example.utils.ConstUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Chat {

    private static final int intervalSecond = 3;

    private static final List<String> ROOM_ID_LIST = Lists.newArrayList(
            ChatGroup.CHAT_TEST.getRoomId(),
            ChatGroup.CREATE_ACCOUNT.getRoomId(),
            ChatGroup.CREATE_ZUOXI_ACCOUNT.getRoomId(),
            ChatGroup.ZHEYUN.getRoomId()
    );
    private static final Set<String> IGNORED_CREATOR_SET = Sets.newHashSet("HuangPeng", "wbeoDzcAAAhV_7dkJRI1DgihZC6K7JPQ");
    private static final Set<String> ADMIN_CREATOR_SET = Sets.newHashSet("HuangPeng", "WangQi", "LuoQiJia", "ChenZhengWei", "GengRunChen", "ShiLin", "ZhangJiaWei", "WangYi", "ZhaoDi", "FanShiXiong", "PengRong", "LiuDaMing");
    public static final Set<ChatGroup> JIAFANG_CHAT_GROUP_SET = Sets.newHashSet(ChatGroup.ZHEYUN);
    private static final String DOMAIN = Domain.SHANGHUYUNYING;
    private static final String STR_ACCOUNT = ConstUtils.STR_ACCOUNT;
    private static final String ERROR_NOTE = ConstUtils.ERROR_NOTE;

    private static void updateAccountToSlotRule() {
        try {
            Map<String, String> mainAccount2GroupId = BaizeClientFactory.getBaizeClient().adminGetMainAccount2GroupId();
            Set<String> accountSet = mainAccount2GroupId.keySet();
            if (accountSet.size() > 0) {
                Set<Keyword> keywordSet = accountSet.stream()
                        .map(account -> new Keyword(STR_ACCOUNT, account.toLowerCase(), account.toLowerCase(), 1.0f, "custom_" + DOMAIN))
                        .collect(Collectors.toSet());
                Set<String> domainSet = Sets.newHashSet(DOMAIN);
                AbstractSlotRule whitelistSLotRule = new WhitelistSlotRule(keywordSet, domainSet);
                SlotRuleDetect.resetCustomSlotRuleList(DOMAIN, Lists.newArrayList(whitelistSLotRule));
            }
        } catch (Exception e) {
            e.printStackTrace();
            MsgUtils.sendQiweiWarning("updateAccountToSlotRule error: " + e);
        }
    }

    public static void process(List<MsgRecord> msgRecordList) {
        if (msgRecordList.size() > 0) {
            updateAccountToSlotRule();
            for (MsgRecord msgRecord : msgRecordList) {
                ChatGroup chatGroup = msgRecord.getChatGroup();
                String creator = msgRecord.getMsgFrom();
                creator = CreatorUtils.map(creator);
                // 同一企业内容为userid，非相同企业为external userid
                // 机器人与外部联系人的账号都是external userid，其中机器人的external userid是以"wb"开头，例如:"wbjc7bDWAAJVYIUKpSA3Z5U11tDO4AAA"，外部联系人的external userid以"wo"或"wm"开头。
                if (IGNORED_CREATOR_SET.contains(creator)
                        || creator.startsWith("wb") // 机器人的external userid是以"wb"开头
                        || (JIAFANG_CHAT_GROUP_SET.contains(chatGroup) && ADMIN_CREATOR_SET.contains(creator))
                ) {
                    continue;
                } else {
                    try {
                        String msgTime = msgRecord.getMsgTime();
                        String content = msgRecord.getContent();
                        String quotation = new StringBuilder()
                                .append("> ").append("<font color=\"comment\">").append(creator).append(" ").append(msgTime).append("</font>").append("\n")
                                .append("> ").append("<font color=\"comment\">").append(String.join("</font>\n> <font color=\"comment\">", content.split("\n"))).append("</font>")
                                .toString();
                        List<String> phoneList = Lists.newArrayList(creator);
                        List<AbstractInstructionBean> instructionBeanList = InstructionEngine.getInstructionBeanList(DOMAIN, content, chatGroup, creator);
//                    System.out.println(content + " " + instructionType2instructionBeanList.size());
                        if (CollectionUtils.isEmpty(instructionBeanList)) {
                            //                            String msgToSend = who + " " + msgTime + "\n" + "（识别为空）";
                            //                            MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.TEXT, Collections.emptyList());
                            String msgToSend = "（识别为空）" + "\n" + quotation;
                            MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.MARKDOWN, phoneList);
                            MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), ERROR_NOTE, MsgType.TEXT, phoneList);
                        } else {
                            for (AbstractInstructionBean instructionBean : instructionBeanList) {
                                InstructionType instructionType = instructionBean.getInstructionType();
                                if (instructionType == InstructionType.ACTION_EXECUTE_INSTRUCTION
                                        || instructionType == InstructionType.ACTION_REMOVE_INSTRUCTION) {
                                    ExecuteInstructionBean executeInstructionBean = (ExecuteInstructionBean) instructionBean;
                                    String instructionIdToExecute = executeInstructionBean.getInstructionIdToExecute();
                                    switch (instructionType) {
                                        case ACTION_REMOVE_INSTRUCTION: {
                                            AbstractInstructionBean instructionBeanToExecute = ExecuteInstructionUtils.removeUnexecutedInstructionBean(instructionIdToExecute);
                                            if (instructionBeanToExecute == null) {
                                                String msgToSend = instructionBean.toDescription() + "\n" + "（未找到该指令）" + "\n" + quotation;
                                                MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.MARKDOWN, phoneList);
                                                MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), ERROR_NOTE, MsgType.TEXT, phoneList);
                                            } else {
                                                String msgToSend = instructionBean.toDescription() + "\n" + "已删除该指令" + "\n" + quotation;
                                                MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.MARKDOWN, Collections.emptyList());

                                                ExecuteInstructionUtils.reportAll(chatGroup, chatGroup.getRobotToken());
                                            }
                                            break;
                                        }
                                        case ACTION_EXECUTE_INSTRUCTION: {
                                            AbstractInstructionBean instructionBeanToExecute = ExecuteInstructionUtils.getUnexecutedInstructionBean(instructionIdToExecute);
                                            if (instructionBeanToExecute == null) {
                                                String msgToSend = instructionBean.toDescription() + "\n" + "（未找到该指令）" + "\n" + quotation;
                                                MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.MARKDOWN, phoneList);
                                                MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), ERROR_NOTE, MsgType.TEXT, phoneList);
                                            } else {
                                                CheckResult checkResult = instructionBeanToExecute.checkValid();
                                                if (checkResult.isCorrect()) {
                                                    if (Objects.equals(creator, instructionBeanToExecute.getCreator())
                                                            || JIAFANG_CHAT_GROUP_SET.contains(instructionBeanToExecute.getChatGroup())
                                                            || RequestInstructionUtils.isRequestBefore(instructionIdToExecute, creator)) {

                                                        String msgToSend = instructionBean.toDescription() + "\n" + instructionBeanToExecute.toDescription() + "\n" + quotation;
                                                        MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.MARKDOWN, Collections.emptyList());

                                                        instructionBeanToExecute.asyncExecute(chatGroup.getRobotToken());
                                                    } else {
                                                        RequestInstructionUtils.put(instructionIdToExecute, creator);

                                                        String msgToSend = "【非本人创建的指令，需二次确认，请再输入一遍执行命令】" + "\n" + instructionBeanToExecute.toDescription();
                                                        MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.TEXT, phoneList);
                                                    }
                                                } else {
                                                    String msgToSend = "执行指令失败！" + checkResult.getMsg() + "\n" + instructionBeanToExecute.toDescription();
                                                    ExecuteInstructionUtils.removeUnexecutedInstructionBean(instructionBeanToExecute.getInstructionId());
                                                    MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.TEXT, phoneList);
                                                }
                                            }
                                            break;
                                        }
                                        default:
                                            break;
                                    }
                                } else if (instructionType == InstructionType.ACTION_CHECK_TASK_DATA
                                        || instructionType == InstructionType.ACTION_DOWNLOAD_AUDIO
                                        || instructionType == InstructionType.ACTION_REPORT_INSTRUCTION
                                        || instructionType == InstructionType.ACTION_REPORT_FINISHED_INSTRUCTION
                                        || instructionType == InstructionType.ACTION_REPORT_CALLED_TASK_TEMPLATE
                                        || instructionType == InstructionType.ACTION_REPORT_TASK_STATISTIC
                                        || instructionType == InstructionType.ACTION_REPORT_TASK_SCRIPT_STATISTIC
                                        || instructionType == InstructionType.ACTION_REPORT_FORBID_DISTRICT_ALL_DAY
                                        || instructionType == InstructionType.ACTION_GET_TASK_TEMPLATE
                                        || instructionType == InstructionType.ACTION_REPORT_ACCOUNT_LOCKED_CONCURRENCY
                                        || instructionType == InstructionType.ACTION_REPORT_RECENTLY_UPDATED_SCRIPT_STATISTIC
                                        || instructionType == InstructionType.ACTION_REPORT_SCRIPT_FOR_LINE
                                        || instructionType == InstructionType.ACTION_REPORT_SCRIPT_FOR_JIAFANG) {
//                                        CallableInfo callableInfo = instructionBean.getCallableInfo();
//                                        ExecutionResult executionResult = callableInfo.getCallable().call();
//                                        String msgToSend = executionResult.getMsg();
//                                        if (executionResult.isSuccess()) {
//                                            MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.MARKDOWN, Collections.emptyList());
//                                        } else {
//                                            MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.TEXT, phoneList);
//                                        }
                                    ExecuteInstructionUtils.immediatelyExecuteInstructionBean(instructionBean);
                                } else {
                                    if (instructionType == InstructionType.ACTION_CREATE_MAIN_ACCOUNT
                                            && chatGroup != ChatGroup.CREATE_ACCOUNT) {
                                        String msgToSend = "禁止在本群新开主账号，请立即撤回消息！！！";
                                        MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.TEXT, phoneList);
                                        continue;
                                    }
                                    ExecuteInstructionUtils.addUnexecutedInstructionBean(instructionBean);
                                    String instruction = instructionBean.toDescription();
//                                        String msgToSend = who + " " + msgTime + "\n" + instruction;
//                                        MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.TEXT, Collections.emptyList());
                                    String msgToSend = "【指令创建成功，请确认执行】\n" + instruction + "\n" + quotation;
                                    MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msgToSend, MsgType.MARKDOWN, Collections.emptyList());
//                                        System.out.println(msgToSend);
                                }
                            }
                        }
                    } catch(Exception e) {
                        System.out.println(msgRecord.getMsgTime());
                        e.printStackTrace();
                        MsgUtils.sendQiweiWarning("process msgRecordList error: " + e);
                    }
                }
            }
        }

    }

    public static void everydayPreparation() {
        ExecuteInstructionUtils.clearInstructionBean();
        ForbiddenDistrictUtils.clearAccount2districtBeanInfo();
        RequestInstructionUtils.clear();
    }

    public static void start() {
        MsgListener listener = new MsgListener(ROOM_ID_LIST, intervalSecond, x-> process(x), Chat::everydayPreparation);
        listener.start();
    }
}
