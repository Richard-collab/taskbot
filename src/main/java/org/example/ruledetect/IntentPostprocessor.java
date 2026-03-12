package org.example.ruledetect;

import org.example.instruction.bean.InstructionType;
import org.example.ruledetect.bean.IntentInfo;
import org.example.ruledetect.bean.Slot;
import org.example.utils.ConstUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntentPostprocessor {

    private static final double score = 1.0;
    private static final String EVIDENCE_VALUE = "IntentPostprocessor";

    private static final String STR_ACTION_START_TASK = InstructionType.ACTION_START_TASK.getName();

    public static List<IntentInfo> postprocess(List<IntentInfo> originalIntentInfoList, List<Slot> slotList) {
        List<IntentInfo> intentInfoList = new ArrayList<>(originalIntentInfoList);
        List<String> intentList = intentInfoList.stream()
                .map(intentInfo -> intentInfo.getIntent()).collect(Collectors.toList());
        List<String> slotNameList = slotList.stream()
                .map(slot -> slot.getSlotName()).collect(Collectors.toList());
        if (!intentList.contains(STR_ACTION_START_TASK)
                && slotNameList.contains(ConstUtils.STR_EXPECTED_START_TIME)
                && slotNameList.contains(ConstUtils.STR_EXPECTED_END_TIME)) {
            IntentInfo intentInfo = new IntentInfo(STR_ACTION_START_TASK, score, EVIDENCE_VALUE);
            intentInfoList.add(intentInfo);
        }
        return intentInfoList;
    }

}
