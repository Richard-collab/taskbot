package org.example.chat.bean.baize;

public enum EnableStatus {

    ENABLE("启用"),
    DISABLE("停用"),
    ;

    private String caption;

    EnableStatus(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public static EnableStatus fromCaption(String caption) {
        if (caption == null) {
            return null;
        }
        EnableStatus result = null;
        switch (caption) {
            case "启用":
            case "开启": {
                result = EnableStatus.ENABLE;
                break;
            }
            case "停用":
            case "关闭": {
                result = EnableStatus.DISABLE;
                break;
            }
            default:
                break;
        }
        return result;
    }
}
