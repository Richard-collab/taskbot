package org.example.chat.bean.baize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class AudioInfo implements Serializable {

    private String audioUrl;
    private String lineCode;
    private String callOutTime;
    private int callDurationSec;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
