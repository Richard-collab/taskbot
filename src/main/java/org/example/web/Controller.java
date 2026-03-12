package org.example.web;

import com.google.common.collect.Lists;
import io.jooby.MediaType;
import io.jooby.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.chat.Chat;
import org.example.chat.bean.ChatGroup;
import org.example.chat.bean.MsgRecord;
import org.example.chat.utils.MonitorUtils;
import org.example.utils.DatetimeUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/app")
public class Controller {

    private static final String CREATOR = "web";

//    @GET("/get")
//    public String sayHi() {
//        return "Hello Mvc!";
//    }

    @POST("/send")
    @Consumes(MediaType.JSON)//可省略
    @Produces(MediaType.JSON)//可省略
    public Response send(Request request) {
        try {
            String content = request.getContent();
            if ("shutdown this app right now".equals(content)) {
                MonitorUtils.apply(() -> System.exit(0));
                return new Response("100", "执行失败！请重试");
            }
            ChatGroup chatGroup = ChatGroup.CHAT_TEST;
            String msgId = UUID.randomUUID().toString().replace("-", ""); // 生成随机UUID
            String msgTime = DatetimeUtils.getStrDatetime(new Date());
            List<MsgRecord> msgRecordList = Lists.newArrayList(new MsgRecord(chatGroup.getRoomId(), msgId, CREATOR, msgTime, content));
            Chat.process(msgRecordList);
            return new Response("2000", "执行成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response("100", "执行失败！" + e.getMessage());
        }
    }

    @Getter
    @Setter
    class Request {
        private String content;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class Response {
        private String code;
        private String msg;
    }
}
