package org.example.instruction.bean;

import org.example.utils.JsonUtils;

import java.io.Serializable;

public class ExecutionResult implements Serializable {

    private boolean success;
    private String msg;

    public ExecutionResult(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
