package org.example.chat.bean.baize;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.instruction.bean.Operator;
import org.example.utils.JsonUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class SupplyLineGroup implements Serializable {

    private Integer id;
    private String createTime;
    private String updateTime;
    private String tenantLineNumber;
    private Operator serviceProvider;
    private List<String> cityCodes;
    private List<String> supplyLineNumbers;
    private Integer maxConcurrentLimit;


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
