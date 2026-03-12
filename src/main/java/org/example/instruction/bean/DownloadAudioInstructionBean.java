package org.example.instruction.bean;

import com.google.common.collect.Lists;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.baize.AudioInfo;
import org.example.chat.bean.baize.TaskType;
import org.example.chat.utils.BaizeClient;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.MsgUtils;
import org.example.instruction.utils.InstructionUtils;
import org.example.utils.CollectionUtils;
import org.example.utils.DatetimeUtils;
import org.example.utils.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class DownloadAudioInstructionBean extends AbstractInstructionBean {

    private static final InstructionType INSTRUCTION_TYPE = InstructionType.ACTION_DOWNLOAD_AUDIO;
    private static final Random RANDOM = new Random();

    private String account;
    private String product;
    private String entranceMode;
    private Set<TaskType> taskTypeSet;
    private String phone;
    private String datetime;

    private String tenantLine;
    private String callingNumber;

    public DownloadAudioInstructionBean(
            String instructionId, ChatGroup chatGroup, String creator, String account, String product, String entranceMode,
            Set<TaskType> taskTypeSet, String phone, String datetime, String tenantLine, String callingNumber) {
        super(instructionId, INSTRUCTION_TYPE, chatGroup, creator);
        this.account = account;
        this.product = product;
        this.entranceMode = entranceMode;
        this.taskTypeSet = taskTypeSet;
        this.phone = phone;
        this.datetime = datetime;
        this.tenantLine = tenantLine;
        this.callingNumber = callingNumber;
    }

    @Override
    public String toDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("下载录音 ");
        if (!StringUtils.isEmpty(account)) {
            sb.append(account).append(" ");
        }
        if (!StringUtils.isEmpty(product) || !StringUtils.isEmpty(entranceMode)) {
            if (!StringUtils.isEmpty(product)) {
                sb.append(product);
            }
            if (!StringUtils.isEmpty(entranceMode)) {
                sb.append(entranceMode);
            }
            sb.append(" ");
        }
        if (!CollectionUtils.isEmpty(taskTypeSet)) {
            sb.append(taskTypeSet.stream().map(taskType -> taskType.getCaption()).collect(Collectors.joining("、"))).append("任务 ");
        }
        if (!StringUtils.isEmpty(phone)) {
            sb.append(phone).append(" ");
        }
        if (!StringUtils.isEmpty(datetime)) {
            sb.append(datetime).append(" ");
        }
        if (!StringUtils.isEmpty(tenantLine)) {
            sb.append(tenantLine).append("线路");
        }
        return sb.toString();
    }

    @Override
    public List<AbstractInstructionBean> getSubInstructionBeanList() {
        return Lists.newArrayList(this);
    }

    @Override
    public CheckResult checkValid() {
        if (StringUtils.isEmpty(phone)) {
            return new CheckResult(false, "号码为空");
        }
        if (StringUtils.isEmpty(datetime))  {
            return new CheckResult(false, "外呼时间为空");
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
                    String startDatetime = null;
                    String endDateTime = null;
                    if (!StringUtils.isEmpty(datetime)) {
                        if (datetime.length() <= 10) {
                            startDatetime = datetime + " 00:00:00";
                            endDateTime = datetime + " 23:59:59";
                        } else {
                            Date date = DatetimeUtils.getDatetime(datetime + ":00");
                            Date startDate = DatetimeUtils.addMinute(date, -5);
                            Date endDate = DatetimeUtils.addMinute(date, 5);
                            startDatetime = DatetimeUtils.getStrDatetime(startDate);
                            endDateTime = DatetimeUtils.getStrDatetime(endDate);
                        }
                    }
                    List<AudioInfo> audioInfoList = client.adminGetAudioUrlList(phone, account, taskTypeSet, startDatetime, endDateTime, null);
                    audioInfoList = audioInfoList.stream()
                            .filter(audioInfo -> InstructionUtils.isLine(audioInfo.getLineCode(), tenantLine, callingNumber))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(audioInfoList)) {
                        String msg = "执行指令失败！找不到对应的音频" + "\n" + this.toDescription();
                        return new ExecutionResult(false, msg);
                    } else {
                        List<String> failedAudioUrlList = new ArrayList<>();
                        for (AudioInfo audioInfo : audioInfoList) {
                            String audioUrl = audioInfo.getAudioUrl();
//                            audioUrl = audioUrl.replace(".com/", ".cn/");
                            audioUrl = audioUrl.replace("http://ai.system.bountech.com", "http://192.168.215.73:8333");
//                            String[] item = new File(audioUrl).getName().split("\\.");
//                            String pathOutput = Paths.get("data/audio/", phone + "_" + audioInfo.getCallOutTime().replaceAll("[ :-]", "") + "_" + RANDOM.nextInt(100000) + "." + item[1]).toString();
                            String audioName = new File(audioUrl).getName();
                            String pathOutput = Paths.get("data/audio/", phone + "_" + audioInfo.getCallOutTime().replaceAll("[ :-]", "") + "_" + RANDOM.nextInt(100000) + "_" + audioName).toString();
                            boolean access = client.download(audioUrl, pathOutput);
                            if (!access) {
                                failedAudioUrlList.add(audioUrl);
                            } else {
                                ChatGroup chatGroup = this.getChatGroup();
                                boolean fileSuccess = MsgUtils.sendQiweiFile(chatGroup.getRobotToken(), pathOutput);
                                if (!fileSuccess) {
                                    failedAudioUrlList.add(audioInfo.getAudioUrl());
                                }
                            }
                        }
                        if (failedAudioUrlList.isEmpty()) {
//                            String msg = "共拉取到" + audioInfoList.size() + "条音频";
                            String msg = "";
                            return new ExecutionResult(true, msg);
                        } else {
                            String msg = "以下音频拉取失败：\n" + String.join("\n", failedAudioUrlList);
                            return new ExecutionResult(false, msg);
                        }
                    }
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
