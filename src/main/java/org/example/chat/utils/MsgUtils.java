package org.example.chat.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import org.example.chat.bean.*;
import org.example.utils.*;
import org.example.utils.bean.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MsgUtils {

    private static final int CONNECT_TIMEOUT = 10 * 1000;
    private static final int READ_TIMEOUT = 10 * 1000;
    private static final String PULL_WECHAT_MSG_URL = ConstUtils.PULL_WECHAT_MSG_URL;
    private static final String SEND_QIWEI_MSG_URL = ConstUtils.SEND_QIWEI_MSG_URL;
    private static final String SEND_QIWEI_FILE_URL = ConstUtils.SEND_QIWEI_FILE_URL;
    private static final String QIWEI_TOKEN_ERROR = ConstUtils.QIWEI_TOKEN_ERROR;
    private static final String SEND_LOCAL_MSG_URL = ConstUtils.SEND_LOCAL_MSG_URL;

    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static boolean isSendLocalMsg = false;
    private static final String LOCAL_QIWEI_TOKEN = ConstUtils.LOCAL_QIWEI_TOKEN;

    public static List<MsgRecord> pullMsgs(List<String> roomIdList, String startTime, String endTime) {
        MsgRequest request = new MsgRequest(roomIdList, startTime, endTime);
        HttpResponse rsp = HttpUtils.doPost(PULL_WECHAT_MSG_URL, request, HttpUtils.POST_JSON_HEADER_MAP, CONNECT_TIMEOUT, READ_TIMEOUT);
        if (rsp.getCode() == 200) {
            try {
                MsgResponse msgRsp = JsonUtils.fromJson(rsp.getText(), new TypeToken<MsgResponse>() {
                });
                if ("2000".equals(msgRsp.getCode())) {
                    return msgRsp.getRecordList();
                } else {
                    sendQiweiWarning("pull wechat msg error: " + msgRsp.getMsg());
                    return null;
                }
            } catch (Exception e) {
                sendQiweiWarning("pull wechat msg error: " + e);
                return null;
            }
        } else {
            sendQiweiWarning("pull wechat msg error: code " + rsp.getCode());
            return null;
        }
    }

    public static boolean sendQiweiMsg(String token, String msg, String msgType, Collection<String> mentionedList) {
        if (StringUtils.isEmpty(msg)) {
            return true;
        }

        if (BaseUtils.isLocal()) {
            System.out.println("sendQiweiMsg:\n" + msg + "\n");
            if (isSendLocalMsg) {
                Runnable runnable = () -> sendLocalMsg(LOCAL_QIWEI_TOKEN, msg, msgType, mentionedList);
                executor.submit(runnable);
            }
            return true;
        } else {
//            System.out.println("sendQiweiMsg:\n" + msg + "\n");
            Runnable runnable = () -> send(token, msg, msgType, mentionedList);
            executor.submit(runnable);
            return true;
        }
    }

    private static boolean send(String token, String msg, String msgType, Collection<String> mentionedList) {
        try {
            if (MsgType.TEXT.equals(msgType) && msg.getBytes(StandardCharsets.UTF_8).length > 2048) {
                // text内容，最长不超过2048个字节
                msgType = MsgType.MARKDOWN_V2;
            }
            List<String> textList;
            if ((MsgType.MARKDOWN.equals(msgType) || MsgType.MARKDOWN_V2.equals(msgType))
                    && msg.getBytes(StandardCharsets.UTF_8).length > 4096) {
                // markdown、markdown_v2内容，最长不超过4096个字节
                textList = StringSplitterByByteLength.splitLineByByteLength(msg, 4096);
            } else {
                textList = Lists.newArrayList(msg);
            }
            boolean success = true;
            for (String text : textList) {
                MsgRateLimiter.acquire(token); // 企微限流
                Map<String, Object> request = ImmutableMap.of("token", token, "content", text, "msgType", msgType, "mentionedList", mentionedList);
                HttpResponse rsp = HttpUtils.doPost(SEND_QIWEI_MSG_URL, request, HttpUtils.POST_JSON_HEADER_MAP, CONNECT_TIMEOUT, READ_TIMEOUT);
                if (rsp.getCode() == 200) {
                    Map<String, Object> msgRsp = JsonUtils.fromJson(rsp.getText(), new TypeToken<Map<String, Object>>() {
                    });
                    if ("2000".equals(msgRsp.get("code"))) {
//                            return true;
                    } else {
                        System.out.println("sendQiweiMsg error: " + rsp);
//                            return false;
                        success = false;
                    }
                } else {
                    System.out.println("sendQiweiMsg error: " + rsp);
//                        return false;
                    success = false;
                }
            }
            return success;
        } catch (Exception e) {
            System.out.println("sendQiweiMsg error: " + token + " " + msg + "\n" + e);
            return false;
        }
    }

    public static boolean sendQiweiWarning(String msg) {
        return sendQiweiWarning(msg, Collections.emptyList());
    }

    public static boolean sendQiweiWarning(String msg, List<String> phoneList) {
        return sendQiweiMsg(QIWEI_TOKEN_ERROR, msg, MsgType.TEXT, phoneList);
    }

    public static boolean sendQiweiFile(String token, String pathFile) {
        return sendQiweiFile(token, pathFile, MsgType.FILE);
    }

    public static boolean sendQiweiImage(String token, String pathFile) {
        return sendQiweiFile(token, pathFile, MsgType.IMAGE);
    }


    public static boolean sendQiweiVoice(String token, String pathFile) {
        return sendQiweiFile(token, pathFile, MsgType.VOICE);
    }

    private static boolean sendQiweiFile(String token, String pathFile, String msgType) {
        if (BaseUtils.isLocal()) {
            System.out.println("sendQiweiFile:\n" + pathFile + "\n");
            return true;
        } else {
            try {
                MsgRateLimiter.acquire(token); // 企微限流
                Map<String, String> request = ImmutableMap.of("token", token, "type", msgType);
                Map<String, String> fileMap = ImmutableMap.of("file", pathFile);
                HttpResponse rsp = HttpUtils.doMultiPartFormPartPost(SEND_QIWEI_FILE_URL, request, fileMap);
                if (rsp.getCode() == 200) {
                    Map<String, Object> msgRsp = JsonUtils.fromJson(rsp.getText(), new TypeToken<Map<String, Object>>() {
                    });
                    if ("2000".equals(msgRsp.get("code"))) {
                        return true;
                    } else {
                        System.out.println("sendQiweiFile error: " + rsp);
                        sendQiweiWarning("sendQiweiFile error: " + rsp);
                        return false;
                    }
                } else {
                    System.out.println("sendQiweiFile error: " + rsp);
                    sendQiweiWarning("sendQiweiFile error: " + rsp);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("sendQiweiFile error: " + token + " " + pathFile + " " + e);
                sendQiweiWarning("sendQiweiFile error: " + token + " " + pathFile + " " + e);
                return false;
            }
        }
    }


    private static boolean sendLocalMsg(String token, String msg, String msgType, Collection<String> mentionedList) {

        try {
            if (MsgType.TEXT.equals(msgType) && msg.getBytes(StandardCharsets.UTF_8).length > 2048) {
                // text内容，最长不超过2048个字节
                msgType = MsgType.MARKDOWN_V2;
            }
            List<String> textList;
            if ((MsgType.MARKDOWN.equals(msgType) || MsgType.MARKDOWN_V2.equals(msgType))
                    && msg.getBytes(StandardCharsets.UTF_8).length > 4096) {
                // markdown、markdown_v2内容，最长不超过4096个字节
                textList = StringSplitterByByteLength.splitLineByByteLength(msg, 4096);
            } else {
                textList = Lists.newArrayList(msg);
            }
            boolean success = true;
            for (String text : textList) {
                MsgRateLimiter.acquire(token); // 企微限流
                Map<String, Object> request = ImmutableMap.of(
                        "msgtype", msgType,
                        msgType, ImmutableMap.of("content", text, "mentioned_list", mentionedList));
                String url = SEND_LOCAL_MSG_URL + "?key=" + token;
                HttpResponse rsp = HttpUtils.doPost(url, request, HttpUtils.POST_JSON_HEADER_MAP, CONNECT_TIMEOUT, READ_TIMEOUT);
                if (rsp.getCode() == 200) {
                    Map<String, Object> msgRsp = JsonUtils.fromJson(rsp.getText(), new TypeToken<Map<String, Object>>() {
                    });
                    if (NumUtils.equals((Double) msgRsp.get("errcode"), 0)) {
//                            return true;
                    } else {
                        System.out.println("sendQiweiMsg error: " + rsp);
//                            return false;
                        success = false;
                    }
                } else {
                    System.out.println("sendQiweiMsg error: " + rsp);
//                        return false;
                    success = false;
                }
            }
            return success;
        } catch (Exception e) {
            System.out.println("sendQiweiMsg error: " + token + " " + msg + "\n" + e);
            return false;
        }

    }


    public static void main(String[] args) {
        List<String> roomIdList = Lists.newArrayList(ChatGroup.TEST.getRoomId());
        String startTime = "2025-05-27 09:00:00";
        String endTime = "2025-05-27 17:00:00";
        List<MsgRecord> recordList = pullMsgs(roomIdList, startTime, endTime);
        System.out.println(recordList);
    }
}
