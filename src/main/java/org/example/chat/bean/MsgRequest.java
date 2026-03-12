package org.example.chat.bean;

import com.google.gson.annotations.SerializedName;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

public class MsgRequest implements Serializable {

    @SerializedName("roomIds")
    private List<String> roomIdList;
    @SerializedName("startTime")
    private String startTime; // yyyy-MM-dd HH:mm:ss
    @SerializedName("endTime")
    private String endTime; // yyyy-MM-dd HH:mm:ss

    public MsgRequest(List<String> roomIdList, String startTime, String endTime) {
        this.roomIdList = roomIdList;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public List<String> getRoomIdList() {
        return roomIdList;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
