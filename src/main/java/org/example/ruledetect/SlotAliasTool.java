package org.example.ruledetect;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import org.example.ruledetect.bean.Slot;
import org.example.utils.ConstUtils;
import org.example.utils.JsonUtils;
import org.example.utils.ResourceUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SlotAliasTool implements Serializable {

    private static final SlotAliasTool SLOT_ALIAS_TOOL = SlotAliasTool.getRuleFromJsonFile(ResourceUtils.getAbsolutePath("/slot_alias_rules.json"));
    private static final Set<String> AVAILABLE_SLOT_NAME_SET = Sets.newHashSet(
            ConstUtils.STR_PRODUCT,
            ConstUtils.STR_TENANT_LINE,
            ConstUtils.STR_ENTRANCE_MODE,
            ConstUtils.STR_ACCOUNT,
            ConstUtils.STR_PROVINCE,
            ConstUtils.STR_CITY,
            ConstUtils.STR_FORBIDDEN_PROVINCE,
            ConstUtils.STR_FORBIDDEN_CITY
    );

    private Map<String, String> aliasMap;

    public SlotAliasTool(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public static final SlotAliasTool getInstance() {
        return SLOT_ALIAS_TOOL;
    }

    public void normalize(Slot slot) {
        String rawValue = slot.getRawValue();
        String slotName = slot.getSlotName();
        if (aliasMap.containsKey(rawValue)
            && AVAILABLE_SLOT_NAME_SET.contains(slotName)) {
//                && !(startIndex - 1 >= 0 && startIndex - 1 < rawQuery.length() && Objects.equals("{", rawQuery.substring(startIndex - 1, startIndex)) && endIndex >= 0 && endIndex < rawQuery.length() && Objects.equals("}", rawQuery.substring(endIndex, endIndex + 1)))) {
            String normedValue = aliasMap.get(rawValue);
            slot.setNormedValue(normedValue);
        }
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

    private static SlotAliasTool getRuleFromJsonFile(String pathInputJson) {
        SlotAliasTool tool = null;
        try {
            String text = new String(Files.readAllBytes(Paths.get(pathInputJson)), StandardCharsets.UTF_8);
            Map<String, List<String>> map = JsonUtils.fromJson(text, new TypeToken<Map<String, List<String>>>(){});
            Map<String, String> aliasMap = new HashMap<>();
            map.forEach((normalizedName, aliasList) -> {
                for (String aliasName: aliasList) {
                    aliasMap.put(aliasName, normalizedName);
                }
            });
            tool = new SlotAliasTool(aliasMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tool;
    }
}
