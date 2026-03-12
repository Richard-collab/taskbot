package org.example.ruledetect.bean;

import org.example.utils.JsonUtils;

import java.io.Serializable;

public class IntentInfo implements Serializable {

    private String intent;
    private boolean positive;
    private double score;
    private String evidence; //正则、例句等

    public IntentInfo(String intent, boolean positive, double score, String evidence) {
        this.intent = intent;
        this.positive = positive;
        this.score = score;
        this.evidence = evidence;
    }

    public IntentInfo(String intent, double score, String evidence) {
        this.intent = intent;
        this.positive = true;
        this.score = score;
        this.evidence = evidence;
    }

    public String getIntent() {
        return intent;
    }

    public boolean isPositive() {
        return positive;
    }

    public double getScore() {
        return score;
    }

    public String getEvidence() {
        return evidence;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
