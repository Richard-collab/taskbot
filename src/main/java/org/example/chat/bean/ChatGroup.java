package org.example.chat.bean;

import java.util.Objects;

public enum ChatGroup {

    TEST("运营小助手测试沟通群", "wreoDzcAAA679oJzxSz5hB4ap1v4D0Zg", null),
    CHAT_TEST("运营小助手内部使用群", "wreoDzcAAASB4FoQirc7EGzYR7F0i_wg", "3e999abf-d771-4816-b62d-07005285c3cd"),
    OUTBOUND_REPORT("外呼账号运行汇报", null,"9a5f4109-e8a7-4357-8048-85af2afd5334"),
    CREATE_ACCOUNT("账号自动创建群", "wreoDzcAAAsalCYCORcPH8fWXRCHFj-g", "0f1676cc-1651-440b-b9ad-774ea13dff01"),
    CREATE_ZUOXI_ACCOUNT("坐席账号自动创建群", "wreoDzcAAASoSZ9untuHyJ8vUC3MUdNA", "1406ec67-9d32-4810-9377-728bd4735d22"),
    SCRIPT_COMPARATION("新话术运行/对比监控与汇报", "", "203847c1-cbfd-4d73-b470-d8baf4b15c7d"),
    RECENTLY_UPDATED_SCRIPT("新话术上线运行情况汇报", "", "4132ca62-f6e8-412a-b764-edc160634182"),

    //    IMPORT_COMPLAIN_NUMBER("投诉风险号码报告群", "wreoDzcAAAItCUA6ELb_jNVRJ4FVoCxQ"),
    IMPORT_COMPLAIN_NUMBER("投诉风险号码处理结果反馈群", "wreoDzcAAAczSh-Vy5Y12gBIyIiJQnqQ", "47ce71e6-5430-4491-bd2e-f0bee17d5a0c"),
    JINXINTIAN_SAN("白泽-新项目3组合作群", "wreoDzcAAATfxNehq04EAjgTBFnkVjGA", "723fd9d6-4071-4551-8556-7349fd8422c0"), // 这个robotToken是测试群的机器人
    ZHEYUN("BT 浙云业务沟通群", "wreoDzcAAAd9Rcc--4sG8gSeadAyDdvQ", "723fd9d6-4071-4551-8556-7349fd8422c0")
    ;

    private String name;
    private String roomId;
    private String robotToken;

    ChatGroup(String name, String roomId, String robotToken) {
        this.name = name;
        this.roomId = roomId;
        this.robotToken = robotToken;
    }

    public String getName() {
        return name;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRobotToken() {
        return robotToken;
    }

    public static ChatGroup fromRoomId(String roomId) {
        for (ChatGroup chatGroup: ChatGroup.values()) {
            if (Objects.equals(chatGroup.getRoomId(), roomId)) {
                return chatGroup;
            }
        }
        return null;
    }
}
