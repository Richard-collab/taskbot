package org.example.chat.bean.baize;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ScriptSmsTemplate implements Serializable {

    private Integer id;
    private String groupId;
    private EnableStatus templateStatus;
    private String templateName;
    private String secondIndustry;
    private String messageSign;
    @SerializedName("variableUsed")
    private List<SmsVariable> smsVariableList;
    private boolean isPending;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
