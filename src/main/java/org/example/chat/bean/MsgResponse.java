package org.example.chat.bean;

import com.google.gson.annotations.SerializedName;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

public class MsgResponse implements Serializable {

    @SerializedName("code")
    private String code;

    @SerializedName("msg")
    private String msg;

    @SerializedName("data")
    private List<MsgRecord> recordList;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public List<MsgRecord> getRecordList() {
        return recordList;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
