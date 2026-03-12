package org.example.chat.bean.baize;

import java.util.Objects;

public enum OccupyRate {

    LOW("低", 1),
    MIDDLE("中", 3),
    HIGH("高", 6),
    ;

    private String caption;
    private int id;
    OccupyRate(String caption, int id) {
        this.caption = caption;
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public int getId() {
        return id;
    }

    public static OccupyRate fromCaption(String caption) {
        OccupyRate result = null;
        for (OccupyRate value: OccupyRate.values()) {
            if (Objects.equals(value.getCaption(), caption)) {
                result = value;
                break;
            }
        }
        return result;
    }

    public static OccupyRate fromId(Integer id) {
        if (id == null) {
            return null;
        }

        OccupyRate result = null;
        for (OccupyRate value: OccupyRate.values()) {
            if (value.getId() == id) {
                result = value;
                break;
            }
        }
        return result;
    }

}
