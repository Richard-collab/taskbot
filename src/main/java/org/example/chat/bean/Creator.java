package org.example.chat.bean;

public enum Creator {

    ShiLin("13401255178"),
    ZhangJiaWei("17640314768"),
    ChenZhengWei("18605817520"),
    WangYi("13405107392"),
    WangQi("13506132310"),
    LuoQiJia("13183878773"),
    ZhuYaLi("16621051233"),
    CaiXiaoYun("18018199915"),
    LvLeLe("13155718561"),
    WenSheng("13952970709"),
    LiKang("13825259047"),
    ZhaoDi(null),

    Unknown(null),
    ;

    private String phone;

    Creator(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public static Creator fromScriptName(String script) {
        Creator creator = null;
        if (script.contains("F")) {
            creator = Creator.ZhuYaLi;
        } else if (script.contains("L")) {
            creator = Creator.ZhuYaLi;
        } else if (script.contains("C")) {
            creator = Creator.CaiXiaoYun;
        } else if (script.contains("V")) {
            creator = Creator.LvLeLe;
        } else if (script.contains("W")) {
            creator = Creator.WenSheng;
        } else if (script.contains("K")) {
            creator = Creator.LiKang;
        } else if (script.contains("D")) {
            creator = Creator.ZhaoDi;
        } else if (script.contains("S")) {
            creator = Creator.ShiLin;
        } else if (script.contains("Z")) {
            creator = Creator.ChenZhengWei;
        }
        return creator;
    }

    public static Creator fromName(String name) {
        for (Creator creator: Creator.values()) {
            if (creator.name().equals(name)) {
                return creator;
            }
        }
        return Creator.Unknown;
    }
}
