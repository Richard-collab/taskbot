package org.example.instruction.bean;

import lombok.EqualsAndHashCode;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@EqualsAndHashCode
public class DistrictBean implements Serializable {

    private String name;
    private String code;

    public DistrictBean(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
