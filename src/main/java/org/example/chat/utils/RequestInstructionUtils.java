package org.example.chat.utils;

import com.google.gson.reflect.TypeToken;
import org.example.utils.ConstUtils;
import org.example.utils.DatetimeUtils;
import org.example.utils.JsonUtils;
import org.example.utils.SynchronizedMap;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class RequestInstructionUtils {

    private static final String PATH_INSTRUCTION_ID_TO_CREATOR_SET = ConstUtils.PATH_INSTRUCTION_ID_TO_CREATOR_SET;
    private static final SynchronizedMap<String, Set<String>> instructionId2CreatorSet = getSynchronizedMapFromFile(true, PATH_INSTRUCTION_ID_TO_CREATOR_SET);
    private static final int DEFAULT_START_HOUR = MsgListener.DEFAULT_START_HOUR;

    private static SynchronizedMap<String, Set<String>> getSynchronizedMapFromFile(boolean fair, String pathInputJson) {
        Map<String, Set<String>> map = new HashMap<>();
        if (new File(pathInputJson).exists()) {
            try {
                map = JsonUtils.fromJsonFile(pathInputJson, new TypeToken<Map<String, Set<String>>>() {});
            } catch (Exception e) {
            }
        }
        return new SynchronizedMap<>(map, fair, pathInputJson);
    }


    public static void clear() {
        Date nowTime = new Date();
        int nowHour = DatetimeUtils.getHour(nowTime);
        if (0 < nowHour && nowHour < DEFAULT_START_HOUR) {
            instructionId2CreatorSet.clear();
        }
    }

    public static boolean isRequestBefore(String instructionId, String creator) {
        return instructionId2CreatorSet.get(instructionId, set -> set != null && set.contains(creator));
    }

    public static boolean put(String instructionId, String creator) {
        return instructionId2CreatorSet.apply(map -> {
           map.putIfAbsent(instructionId, new HashSet<>());
           boolean flag = map.get(instructionId).add(creator);
           instructionId2CreatorSet.writeToFile();
           return flag;
        });
    }

    public static void main(String[] args) {
        put("id", "creator2");
    }
}
