package org.example.instruction.bean;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class CheckResult implements Serializable {

    private boolean correct;
    private String msg;

    public boolean isError() {
        return !correct;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
