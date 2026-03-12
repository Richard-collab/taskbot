package org.example;

import io.jooby.Jooby;
import org.example.chat.Chat;
import org.example.chat.bean.ChatGroup;
import org.example.chat.utils.BaizeClientFactory;
import org.example.chat.utils.ExecuteInstructionUtils;
import org.example.instruction.InstructionEngine;
import org.example.ruledetect.bean.Domain;
import org.example.web.Application;
import org.example.web.Controller;
import org.example.web.GsonModule;

public class Main {
    public static void main(String[] args) {
//        System.out.println("Hello world!");
//
//        List<String> roomIdList = Lists.newArrayList(ChatGroup.TEST.getRoomId());
//        String startTime = "2025-05-27 09:00:00";
//        String endTime = "2025-05-27 17:00:00";
//        List<MsgRecord> recordList = MsgUtils.pullMsgs(roomIdList, startTime, endTime);
//        System.out.println(recordList);
//        recordList.forEach(System.out::println);
//        System.out.println(ResourceUtils.getAbsolutePath("/slot_rules.json"));


        // warmup
        BaizeClientFactory.warmup();
        InstructionEngine.getInstructionBeanList(Domain.SHANGHUYUNYING, "南宁，成都 极闪融屏蔽", ChatGroup.CHAT_TEST, "creator");
        ExecuteInstructionUtils.getUnexecutedInstructionIdSet();

        // http server
        Jooby.runApp(args, Application::new);

        // start
        System.out.println("start");
        Chat.start();
    }
}