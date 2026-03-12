package org.example.ruledetect.bean;

import java.io.Serializable;

public class Keyword implements Serializable {

    private String slotName;
    private String rawValue;
    private String normedValue;
    private float score;
    private String source;

    public Keyword(String slotName, String rawValue, String normedValue, float score, String source) {
        this.slotName = slotName;
        this.rawValue = rawValue;
        this.normedValue = normedValue;
        this.score = score;
        this.source = source;
    }

    public String getSlotName() {
        return slotName;
    }

    public String getRawValue() {
        return rawValue;
    }

    public String getNormedValue() {
        return normedValue;
    }

    public float getScore() {
        return score;
    }

    public String getSource() {
        return source;
    }
}
