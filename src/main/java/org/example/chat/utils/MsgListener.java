package org.example.chat.utils;

import com.google.gson.reflect.TypeToken;
import org.example.chat.bean.MsgRecord;
import org.example.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class MsgListener {

    private static final String PATH_LAST_TIME_PULL_WECHAT_MSG = ConstUtils.PATH_LAST_TIME_PULL_WECHAT_MSG;
    private static final String PATH_LAST_MSG_PULL_WECHAT_MSG = ConstUtils.PATH_LAST_MSG_PULL_WECHAT_MSG;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final int DEFAULT_START_HOUR = 7;
    private static final int DEFAULT_START_MINUTE = 0;
    private static final int DEFAULT_START_SECOND = 0;
    private static final int DEFAULT_START_MILLISECOND = 0;
    private static final int DELAY_SECOND = 60 * 5;

    private List<String> roomIdList;
    private int intervalSecond;
    private Set<String> lastMsgIdSet;
    private Consumer<List<MsgRecord>> msgProcessor;
    private Runnable everydayPreparation;
    private Date lastTime;

    public MsgListener(List<String> roomIdList, int intervalSecond, Consumer<List<MsgRecord>> msgProcessor, Runnable everydayPreparation) {
        this.roomIdList = roomIdList;
        this.intervalSecond = intervalSecond;
//        this.lastMsgSet = new HashSet<>();
        this.msgProcessor = msgProcessor;
        this.everydayPreparation = everydayPreparation;
        File file = new File(PATH_LAST_TIME_PULL_WECHAT_MSG);
        if (file.exists()) {
            try {
                String strDateTime = FileUtils.readText(PATH_LAST_TIME_PULL_WECHAT_MSG);
                this.lastTime = sdf.parse(strDateTime);
            } catch (Exception e) {

            }
        }
        Date nowTime = new Date();
        if (this.lastTime == null || DatetimeUtils.getDayOfMonth(this.lastTime) != DatetimeUtils.getDayOfMonth(nowTime)) {
            this.lastTime = nowTime;
            this.lastTime = DatetimeUtils.setHour(this.lastTime, DEFAULT_START_HOUR);
            this.lastTime = DatetimeUtils.setMinute(this.lastTime, DEFAULT_START_MINUTE);
            this.lastTime = DatetimeUtils.setSecond(this.lastTime, DEFAULT_START_SECOND);
            this.lastTime = DatetimeUtils.setMillisecond(this.lastTime, DEFAULT_START_MILLISECOND);
        }

        file = new File(PATH_LAST_MSG_PULL_WECHAT_MSG);
        if (file.exists()) {
            try {
                lastMsgIdSet = JsonUtils.fromJsonFile(PATH_LAST_MSG_PULL_WECHAT_MSG, new TypeToken<Set<String>>() {});
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
        if (lastMsgIdSet == null) {
            lastMsgIdSet = new HashSet<>();
        }
    }

    public void start() {
        do {
            try {
                Date nowTime = new Date();
                int nowHour = DatetimeUtils.getHour(nowTime);
                if (0 < nowHour && nowHour < DEFAULT_START_HOUR) {
                    this.lastMsgIdSet.clear();
                    FileUtils.write(JsonUtils.toJson(this.lastMsgIdSet, false), PATH_LAST_MSG_PULL_WECHAT_MSG, false);

                    everydayPreparation.run();
                } else {
                    if (this.lastTime == null || DatetimeUtils.getDayOfMonth(this.lastTime) != DatetimeUtils.getDayOfMonth(nowTime)) {
                        this.lastTime = nowTime;
                    }
                    Date futureTime = DatetimeUtils.addSecond(lastTime, intervalSecond);
                    if (nowTime.after(futureTime)) {
                        try {
                            String startTime = sdf.format(DatetimeUtils.addSecond(lastTime, -DELAY_SECOND));
//                    String endTime = sdf.format(DatetimeUtils.addSecond(futureTime, -DELAY_SECOND + 15));
                            String endTime = sdf.format(DatetimeUtils.addSecond(nowTime, DELAY_SECOND));
                            List<MsgRecord> msgRecordList = MsgUtils.pullMsgs(roomIdList, startTime, endTime);
//                            System.out.println(startTime + " ~ " + endTime + "\n收到消息：\n" + JsonUtils.toJson(msgRecordList, false));
                            msgRecordList = msgRecordList.stream()
                                    .filter(x -> !this.lastMsgIdSet.contains(x.getMsgId())).collect(Collectors.toList());

                            MonitorUtils.increase();

                            msgProcessor.accept(msgRecordList);
//                            System.out.println(startTime + " ~ " + endTime + "\n处理完成");

                            lastTime = futureTime;
                            FileUtils.write(sdf.format(lastTime), PATH_LAST_TIME_PULL_WECHAT_MSG, false);

                            this.lastMsgIdSet.addAll(msgRecordList.stream().map(x -> x.getMsgId()).collect(Collectors.toSet()));
                            FileUtils.write(JsonUtils.toJson(this.lastMsgIdSet, false), PATH_LAST_MSG_PULL_WECHAT_MSG, false);

                            MonitorUtils.decrease();
                        } catch (Exception e) {
                            e.printStackTrace();
                            MsgUtils.sendQiweiWarning("MsgListener error: " + e);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                MsgUtils.sendQiweiWarning("MsgListener error: " + e);
            }
            ThreadUtils.sleep(1 * 1000);
        } while (true);
    }

    public static void main(String[] args) {
        MsgListener msgListener = new MsgListener(null, 1, null, null);
        msgListener.start();
    }
}
