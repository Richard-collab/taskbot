package org.example.utils.bean;

import org.example.utils.JsonUtils;

import java.io.Serializable;

public class HttpResponse implements Serializable {
    private int code;
    private String text;

    public HttpResponse(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
