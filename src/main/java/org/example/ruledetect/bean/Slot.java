package org.example.ruledetect.bean;

import org.example.utils.JsonUtils;

import java.io.Serializable;

public class Slot implements Serializable {

    private String slotName;
    private String rawValue;
    private String normedValue;
    private int startIndex;
    private int endIndex;
    private float score;
    private String source; // 相当于ruleId
    private String evidence;
    private boolean retained;


    public Slot(String slotName, String rawValue, String normedValue, int startIndex, int endIndex, float score, boolean retained, String source, String evidence) {
        this.slotName = slotName;
        this.rawValue = rawValue;
        this.normedValue = normedValue;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.score = score;
        this.retained = retained;
        this.source = source;
        this.evidence = evidence;
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

    public void setNormedValue(String normedValue) {
        this.normedValue = normedValue;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public float getScore() {
        return score;
    }

    public boolean isRetained() {
        return retained;
    }

    public String getSource() {
        return source;
    }

    public String getEvidence() {
        return evidence;
    }

    public int getLength() {
        return endIndex - startIndex;
    }

    public String getKey() {
        return new StringBuilder()
                .append(slotName)
                .append(rawValue)
                .append(startIndex)
                .append(endIndex)
                .append(source)
                .toString();
    }

//    @Override
//    public boolean equals(Object o) {
//        if (!(o instanceof Slot)) {
//            return false;
//        }
//        Slot slot = (Slot) o;
//        return Objects.equals(this.slotName, slot.slotName)
//                && Objects.equals(this.rawValue, slot.rawValue)
//                && Objects.equals(this.normedValue, slot.normedValue)
//                && Objects.equals(this.startIndex, slot.startIndex)
//                && Objects.equals(this.endIndex, slot.endIndex)
//                && Objects.equals(this.score, slot.score)
//                && Objects.equals(this.evidence, slot.evidence);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = 17;
//        result = 37 * result + (this.slotName == null? 0: this.slotName.hashCode());
//        result = 37 * result + (this.slotName == null? 0: this.rawValue.hashCode());
//        result = 37 * result + (this.slotName == null? 0: this.normedValue.hashCode());
//        result = 37 * result + Integer.hashCode(this.startIndex);
//        result = 37 * result + Integer.hashCode(this.endIndex);
//        result = 37 * result + Float.hashCode(this.score);
//        result = 37 * result + (this.slotName == null? 0: this.evidence.hashCode());
//        return result;
//    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
