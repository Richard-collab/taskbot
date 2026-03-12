package org.example.chat.bean;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class MsgRecord implements Serializable {

    @SerializedName("roomId")
    private String roomId;
    @SerializedName("msgId")
    private String msgId;
    @SerializedName("messageFrom")
    private String msgFrom;
    @SerializedName("msgTime")
    private String msgTime;
    @SerializedName("content")
    private String content;

    public ChatGroup getChatGroup() {
        return ChatGroup.fromRoomId(roomId);
    }


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
