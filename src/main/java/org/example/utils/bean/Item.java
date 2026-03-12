package org.example.utils.bean;

import org.example.utils.JsonUtils;

import java.io.Serializable;

public class Item implements Serializable {

    private String value;
    private int startIndex;
    private int endIndex;

    public Item(String value, int startIndex, int endIndex) {
        this.value = value;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getValue() {
        return value;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
