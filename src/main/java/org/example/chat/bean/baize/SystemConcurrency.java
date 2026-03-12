package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
@Setter
public class SystemConcurrency implements Serializable {

    private Integer phoneNum;
    private Integer totalTaskNum;
    private Integer runningTaskNum;
    private Integer putThroughPhoneNum;
    private Integer calledPhoneNum;
    private Integer supplyConcurrent;
    private Integer tenantConcurrent;
    private Integer realConcurrent;
    private Integer pauseConcurrent;
    private Integer triggerSmsNum;
    private Integer sendSmsNum;
    private Integer callRecordNum;
    private Integer callRecordPutThroughNum;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
