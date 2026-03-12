package org.example.ruledetect.bean;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MaskedSlotFilter implements Serializable {

    private static final String LEFT_SIDE = "{{";
    private static final String RIGHT_SIDE = "}}";

    private int startIndex;
    private int endIndex;
    private Map<String, String> filterMap;

    public MaskedSlotFilter(int startIndex, int endIndex, Map<String, String> filterMap) {
        checkValidity(filterMap);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.filterMap = filterMap;
    }

    private static void checkValidity(Map<String, String> filterMap) {
        List<String> fieldNameList = Arrays.stream(Slot.class.getDeclaredFields())
                .map(field -> field.getName()).collect(Collectors.toList());
        filterMap.forEach((key, value) -> {
                Preconditions.checkArgument(fieldNameList.contains(key), "非法的slot属性：" + key);
            });
    }

    public boolean isMatch(Slot slot) {
        boolean flag = true;
        for (Map.Entry<String, String> entry: filterMap.entrySet()) {
            try {
                String key = entry.getKey();
                String value = entry.getValue();
                Field field = Slot.class.getDeclaredField(key);
                field.setAccessible(true);
                String strValue = (String) field.get(slot);
                if (!Objects.equals(strValue, value)) {
                    flag = false;
                    break;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public String toMaskedString() {
        StringBuilder sbMaskedText = new StringBuilder();
        sbMaskedText.append(LEFT_SIDE);
        filterMap.entrySet().stream().sorted(Map.Entry.<String, String>comparingByKey()
                .thenComparing(Map.Entry.comparingByValue())).forEach(entry -> {
            sbMaskedText.append("\"");
            sbMaskedText.append(entry.getKey());
            sbMaskedText.append("\":\"");
            sbMaskedText.append(entry.getValue());
            sbMaskedText.append("\"");
        });
        sbMaskedText.append(RIGHT_SIDE);
        return sbMaskedText.toString()
                .replace("(", "【").replace(")", "】")
                .replace("{{", "〖").replace("}}", "〗");
    }
}
