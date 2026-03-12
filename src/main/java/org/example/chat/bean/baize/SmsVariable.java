package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SmsVariable implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private Integer tenantId;
    private String groupId;
    private String variableName;
    private String variableType;
    private String variableComment;
    private String columnType;
}
