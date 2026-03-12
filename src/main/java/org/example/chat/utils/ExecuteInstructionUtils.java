package org.example.chat.utils;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.chat.Chat;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.MsgType;
import org.example.instruction.bean.*;
import org.example.utils.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ExecuteInstructionUtils {

    private static final String PATH_UNEXECUTED_INSTRUCTION_INFO = ConstUtils.PATH_UNEXECUTED_INSTRUCTION_INFO;
    private static final String PATH_ONGOING_INSTRUCTION_INFO = ConstUtils.PATH_ONGOING_INSTRUCTION_INFO;
    private static final String PATH_FINISHED_INSTRUCTION_INFO = ConstUtils.PATH_FINISHED_INSTRUCTION_INFO;
    private static final SynchronizedMap<String, AbstractInstructionBean> unexecutedInstructionId2instructionBean = getSynchronizedMapFromFile(true, PATH_UNEXECUTED_INSTRUCTION_INFO);
    private static final SynchronizedMap<String, AbstractInstructionBean> ongoingInstructionId2instructionBean = getSynchronizedMapFromFile(true, PATH_ONGOING_INSTRUCTION_INFO);
    private static final SynchronizedMap<String, AbstractInstructionBean> finishedInstructionId2instructionBean = getSynchronizedMapFromFile(true, PATH_FINISHED_INSTRUCTION_INFO);
    private static final int DEFAULT_START_HOUR = MsgListener.DEFAULT_START_HOUR;
    private static final int DEFAULT_END_HOUR = 20;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(15);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final ExecutorService addFinishedExecutor = Executors.newFixedThreadPool(3);
    private static final ScheduledExecutorService addFinishedScheduler = Executors.newScheduledThreadPool(3);
    private static final ExecutorService immediateExecutor = Executors.newFixedThreadPool(5); // 不需要二次确认的命令

    private static final int WARNING_INTERVAL_MIN = 3;

    static {
        scheduler.scheduleAtFixedRate(() -> reportUnexecuted(false, WARNING_INTERVAL_MIN), 0, WARNING_INTERVAL_MIN, TimeUnit.MINUTES);

        Runnable reportTaskTemplate = () -> ReportCalledTaskTemplateInstructionBean.report(ChatGroup.OUTBOUND_REPORT.getRobotToken());
        QuartzUtils.scheduleJob(reportTaskTemplate,"0 00 18 * * ?"); // 每天18:00执行一次
        QuartzUtils.scheduleJob(reportTaskTemplate,"0 30 18 * * ?"); // 每天18:30执行一次

        Runnable reportRecentlyUpdatedScriptStatistic = () -> ReportRecentlyUpdatedScriptStatisticInstructionBean.report(ChatGroup.RECENTLY_UPDATED_SCRIPT.getRobotToken());
        QuartzUtils.scheduleJob(reportRecentlyUpdatedScriptStatistic,"0 00 11 * * ?"); // 每天11:00执行一次
        QuartzUtils.scheduleJob(reportRecentlyUpdatedScriptStatistic,"0 00 17 * * ?"); // 每天17:00执行一次

        QuartzUtils.scheduleJob(() -> ReportFinishedInstructionInstructionBean.report(ChatGroup.CHAT_TEST.getRobotToken()),"0 00 19 * * ?"); // 每天19点执行一次
        QuartzUtils.scheduleJob(() -> ReportFinishedInstructionInstructionBean.report(ChatGroup.CHAT_TEST.getRobotToken()),"0 00 20 * * ?"); // 每天20点执行一次

        QuartzUtils.scheduleJob(() -> reportAccountLockedConcurrency(),"0 */10 * * * ?"); // 每隔10分钟执行一次
    }

    private static SynchronizedMap<String, AbstractInstructionBean> getSynchronizedMapFromFile(boolean fair, String pathInputJson) {
        Map<String, AbstractInstructionBean> map = new HashMap<>();
        if (new File(pathInputJson).exists()) {
            try {
                JsonElement baseElement = JsonUtils.parseJsonFile(pathInputJson);
                baseElement.getAsJsonObject().entrySet().forEach(entry -> {
                    String instructionId = entry.getKey();
                    JsonObject jsonObject = entry.getValue().getAsJsonObject();
                    String strInstructionType = jsonObject.get("instructionType").getAsString();
                    InstructionType instructionType = InstructionType.valueOf(strInstructionType);
                    AbstractInstructionBean instructionBean = (AbstractInstructionBean) JsonUtils.fromJson(jsonObject.toString(), instructionType.getClassType());
                    map.put(instructionId, instructionBean);
                });
            } catch (Exception e) {
            }
        }
        return new SynchronizedMap<>(map, fair, pathInputJson);
    }

    public static void clearInstructionBean() {
        Date nowTime = new Date();
        int nowHour = DatetimeUtils.getHour(nowTime);
        if (0 < nowHour && nowHour < DEFAULT_START_HOUR) {
            unexecutedInstructionId2instructionBean.clear();
            ongoingInstructionId2instructionBean.clear();
            finishedInstructionId2instructionBean.clear();
            MonitorUtils.clear();
        }
    }

    public static void addUnexecutedInstructionBean(AbstractInstructionBean instructionBean) {
        String instructionId = instructionBean.getInstructionId();
        unexecutedInstructionId2instructionBean.put(instructionId, instructionBean);
    }

    public static void addOngoingInstructionBean(AbstractInstructionBean instructionBean) {
        String instructionId = instructionBean.getInstructionId();
        ongoingInstructionId2instructionBean.put(instructionId, instructionBean);
    }

    public static void addFinishedInstructionBean(AbstractInstructionBean instructionBean) {
        String instructionId = instructionBean.getInstructionId();
        finishedInstructionId2instructionBean.put(instructionId, instructionBean);
    }

    public static Set<String> getUnexecutedInstructionIdSet() {
        return unexecutedInstructionId2instructionBean.keySet();
    }

    public static Set<String> getOngoingInstructionIdSet() {
        return ongoingInstructionId2instructionBean.keySet();
    }

    public static Set<String> getFinishedInstructionIdSet() {
        return finishedInstructionId2instructionBean.keySet();
    }


    public static Set<AbstractInstructionBean> getUnexecutedInstructionBeanSet() {
        return unexecutedInstructionId2instructionBean.values();
    }

    public static Set<AbstractInstructionBean> getOngoingInstructionBeanSet() {
        return ongoingInstructionId2instructionBean.values();
    }

    public static Set<AbstractInstructionBean> getFinishedInstructionBeanSet() {
        return finishedInstructionId2instructionBean.values();
    }

    public static AbstractInstructionBean getUnexecutedInstructionBean(String instructionId) {
        return unexecutedInstructionId2instructionBean.get(instructionId);
    }

    public static AbstractInstructionBean getOngoingInstructionBean(String instructionId) {
        return ongoingInstructionId2instructionBean.get(instructionId);
    }

    public static AbstractInstructionBean getFinishedInstructionBean(String instructionId) {
        return finishedInstructionId2instructionBean.get(instructionId);
    }

    public static AbstractInstructionBean removeUnexecutedInstructionBean(String instructionId) {
        return unexecutedInstructionId2instructionBean.remove(instructionId);
    }

    public static AbstractInstructionBean removeOngoingInstructionBean(String instructionId) {
        return ongoingInstructionId2instructionBean.remove(instructionId);
    }

    public static AbstractInstructionBean removeFinishedInstructionBean(String instructionId) {
        return finishedInstructionId2instructionBean.remove(instructionId);
    }

    public static List<Future<ExecutionResult>> executeInstructionBean(String robotToken, String instructionId) {
        // 将命令从存储空间删除，防止重复执行
        AbstractInstructionBean instructionBean = ExecuteInstructionUtils.removeUnexecutedInstructionBean(instructionId);
        if (instructionBean == null) {
            String msg = "执行指令失败！\n指令ID：" + instructionId + "\n该指令不存在或已被执行";
            MsgUtils.sendQiweiMsg(robotToken, msg, MsgType.MARKDOWN, Collections.emptyList());
            return null;
        } else {
            List<Future<ExecutionResult>> futureList = new ArrayList<>();
            List<AbstractInstructionBean> subInstructionBeanList = instructionBean.getSubInstructionBeanList();
            for (AbstractInstructionBean subInstructionBean: subInstructionBeanList) {
                CallableInfo callableInfo = subInstructionBean.getCallableInfo();
                long delaySec = callableInfo.getDelaySec();
                if (delaySec > 0) {
                    String msg = "将在" + (delaySec / 60) + "分钟后执行指令\n" + subInstructionBean.toDescription();
                    MsgUtils.sendQiweiMsg(robotToken, msg, MsgType.MARKDOWN, Collections.emptyList());
                }
                MonitorUtils.increase();
                ExecuteInstructionUtils.addOngoingInstructionBean(subInstructionBean);
                Callable<ExecutionResult> callable = () -> {
                    ExecutionResult result;
                    try {
                        result = callableInfo.getCallable().call();
                    } catch (Exception e) {
                        String msg = "执行指令失败！\n指令ID：" + subInstructionBean.getInstructionId() + "\n" + e + "\n" + subInstructionBean.toDescription();
                        result = new ExecutionResult(false, msg);
                    }
//                if (!result.isSuccess()) {
//                    // 执行没成功的，再把命令加回存储空间
//                    ExecuteInstructionUtils.addInstructionBean(instructionBean);
//                }
                    ExecuteInstructionUtils.removeOngoingInstructionBean(subInstructionBean.getInstructionId());
                    ExecuteInstructionUtils.addFinishedInstructionBean(subInstructionBean);

                    String creator = subInstructionBean.getCreator();
                    if (result.isSuccess()) {
                        MsgUtils.sendQiweiMsg(robotToken, result.getMsg(), MsgType.MARKDOWN, Collections.emptyList());
                    } else {
                        MsgUtils.sendQiweiMsg(robotToken, result.getMsg(), MsgType.TEXT, Lists.newArrayList(creator));
                    }
                    MonitorUtils.decrease();
                    return result;
                };
                boolean isAddFinished = subInstructionBean.getInstructionType() == InstructionType.ACTION_RECALL_TASK;
//                if (isAddFinished) {
//                    System.out.println(DatetimeUtils.getStrDatetime(new Date()) + " " + subInstructionBean.getInstructionId() + " 开始提交复呼任务");
//                }
                Future<ExecutionResult> future;
                if (delaySec <= 0) {
                    if (isAddFinished) {
                        future = addFinishedExecutor.submit(callable);
                    } else {
                        future = executor.submit(callable);
                    }
                } else {
                    if (isAddFinished) {
                        future = addFinishedScheduler.schedule(callable, delaySec, TimeUnit.SECONDS);
                    } else {
                        future = scheduler.schedule(callable, delaySec, TimeUnit.SECONDS);
                    }
                }
//                if (isAddFinished) {
//                    System.out.println(DatetimeUtils.getStrDatetime(new Date()) + " " + subInstructionBean.getInstructionId() + " 复呼任务提交完成");
//                }
                futureList.add(future);
            }
            return futureList;
        }
    }

    public static Future<ExecutionResult> immediatelyExecuteInstructionBean(AbstractInstructionBean instructionBean) {
        Callable<ExecutionResult> callable = () -> {
            ExecutionResult result = instructionBean.getCallableInfo().getCallable().call();

            if (instructionBean.getInstructionType() != InstructionType.ACTION_REMOVE_INSTRUCTION
                    && instructionBean.getInstructionType() != InstructionType.ACTION_REPORT_INSTRUCTION
                    && instructionBean.getInstructionType() != InstructionType.ACTION_REPORT_FINISHED_INSTRUCTION) {
                ExecuteInstructionUtils.addFinishedInstructionBean(instructionBean);
            }

            String msgToSend = result.getMsg();
            String robotToken = instructionBean.getChatGroup().getRobotToken();
            if (result.isSuccess()) {
                if (!StringUtils.isEmpty(msgToSend)) {
                    MsgUtils.sendQiweiMsg(robotToken, msgToSend, MsgType.MARKDOWN, Collections.emptyList());
                }
            } else {
                String creator = instructionBean.getCreator();
                MsgUtils.sendQiweiMsg(robotToken, msgToSend, MsgType.TEXT, Lists.newArrayList(creator));
            }
            MonitorUtils.decrease();
            return result;
        };
        MonitorUtils.increase();
        Future<ExecutionResult> future = immediateExecutor.submit(callable);
        return future;
    }

    /**
     * 展示群聊里的指令
     *
     * @param chatGroup 指定群聊，如果为 null 则展示所有群聊的内容
     */
    public static void reportAll(ChatGroup chatGroup, String robotToken) {
        Set<AbstractInstructionBean> unexecutedInstructionBeanSet = getUnexecutedInstructionBeanSet();
        Set<AbstractInstructionBean> ongoingInstructionBeanSet = getOngoingInstructionBeanSet();
        Set<AbstractInstructionBean> finishedInstructionBeanSet = getFinishedInstructionBeanSet();
        if (chatGroup != null) {
            unexecutedInstructionBeanSet = unexecutedInstructionBeanSet.stream()
                    .filter(bean -> bean.getChatGroup() == chatGroup)
                    .collect(Collectors.toSet());
            ongoingInstructionBeanSet = ongoingInstructionBeanSet.stream()
                    .filter(bean -> bean.getChatGroup() == chatGroup)
                    .collect(Collectors.toSet());
            finishedInstructionBeanSet = finishedInstructionBeanSet.stream()
                    .filter(bean -> bean.getChatGroup() == chatGroup)
                    .collect(Collectors.toSet());
        }

        Map<String, Set<AbstractInstructionBean>> creator2finishedInstructionBeanSet = finishedInstructionBeanSet.stream().collect(
                Collectors.groupingBy(x -> x.getCreator(), LinkedHashMap::new, Collectors.toSet()));
        String msg = new StringBuilder()
                .append("【未执行指令任务】（").append(unexecutedInstructionBeanSet.size()).append("个）\n")
                .append(unexecutedInstructionBeanSet.stream().map(x -> x.getInstructionId() + "（" + x.getCreator() + "）").collect(Collectors.joining("\n")))
                .append("\n\n")
                .append("【进行中指令任务】（").append(ongoingInstructionBeanSet.size()).append("个）\n")
                .append(ongoingInstructionBeanSet.stream().map(x -> x.getInstructionId() + "（" + x.getCreator() + "）").collect(Collectors.joining("\n")))
                .append("\n\n")
                .append("【已完成指令任务】（").append(finishedInstructionBeanSet.size()).append("个）\n")
                .append(creator2finishedInstructionBeanSet.entrySet().stream()
                        .sorted(Comparator.comparing(x -> x.getValue().size(), Comparator.reverseOrder()))
                        .map(x -> x.getValue().size() + "个（" + x.getKey() + "）")
                        .collect(Collectors.joining("\n")))
                .toString();
        MsgUtils.sendQiweiMsg(robotToken, msg, MsgType.TEXT, Collections.emptyList());
    }

    /**
     * 展示未执行的指令任务
     *
     * @param isForceReport
     * @param unexecutedMin
     */
    public static void reportUnexecuted(boolean isForceReport, int unexecutedMin) {
        Set<AbstractInstructionBean> allUnexecutedInstructionBeanSet = getUnexecutedInstructionBeanSet();
        Map<ChatGroup, Set<AbstractInstructionBean>> chatGroup2beanSet = allUnexecutedInstructionBeanSet.stream().collect(
                Collectors.groupingBy(bean -> bean.getChatGroup(), LinkedHashMap::new, Collectors.toSet()));
        chatGroup2beanSet.forEach((chatGroup, unexecutedInstructionBeanSet) -> {
            StringBuilder sb = new StringBuilder().append("【");
            Date nowDate = new Date();
            if (unexecutedMin > 0) {
                unexecutedInstructionBeanSet = unexecutedInstructionBeanSet.stream()
                        .filter(instructionBean -> {
                            String strCreateDatetime = instructionBean.getStrCreateDatetime();
                            Date createDatetime = DatetimeUtils.getDatetime(strCreateDatetime);
                            if (DatetimeUtils.addMinute(createDatetime, unexecutedMin).before(nowDate)) {
                                return true;
                            } else {
                                return false;
                            }
                        }).collect(Collectors.toSet());
                sb.append("超过").append(unexecutedMin).append("分钟");
            }
            sb.append("未执行指令任务】（").append(unexecutedInstructionBeanSet.size()).append("个）\n")
                    .append(unexecutedInstructionBeanSet.stream().map(x -> x.getInstructionId() + "（" + x.getCreator() + "）").collect(Collectors.joining("\n")));
            String msg = sb.toString();
            if (isForceReport || unexecutedInstructionBeanSet.size() > 0) {
                List<String> phoneList = unexecutedInstructionBeanSet.stream()
                        .map(bean -> bean.getCreator())
                        .distinct()
                        .collect(Collectors.toList());
                if (!Chat.JIAFANG_CHAT_GROUP_SET.contains(chatGroup)) {
                    MsgUtils.sendQiweiMsg(chatGroup.getRobotToken(), msg, MsgType.TEXT, phoneList);
                }
            }
        });
    }

    public static void reportAccountLockedConcurrency() {
        Date date = new Date();
        int hour = DatetimeUtils.getHour(date);
        if (hour > DEFAULT_START_HOUR && hour < DEFAULT_END_HOUR) {
            ReportAccountLockedConcurrencyInstructionBean.report(true, ChatGroup.CHAT_TEST.getRobotToken());
        }
    }

}
