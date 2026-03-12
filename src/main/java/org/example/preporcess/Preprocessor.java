package org.example.preporcess;

import org.example.ruledetect.RewriteRuleDetect;
import org.example.ruledetect.SlotEngine;
import org.example.ruledetect.bean.QueryInfo;
import org.example.ruledetect.bean.Slot;
import org.example.utils.ConstUtils;
import org.example.utils.StringUtils;

import java.util.List;

public class Preprocessor {

    private static final String MASKED_SLOT_LEFT_SIDE = ConstUtils.MASKED_SLOT_LEFT_SIDE;
    private static final String MASKED_SLOT_RIGHT_SIDE = ConstUtils.MASKED_SLOT_RIGHT_SIDE;

    public static String getNormalizedQuery(String rawQuery) {
//        String lowerCaseQuery = rawQuery.trim().toLowerCase();
//        String sbcQuery = StringUtils.toSBC(lowerCaseQuery);
        String rewritedQuery = RewriteRuleDetect.rewrite(rawQuery);
        return rewritedQuery;
    }

    public static String getMaskedQuery(String rawQuery) {
        QueryInfo queryInfo = QueryInfo.createQueryInfo(ConstUtils.STR_DOMAIN_MASK, rawQuery, rawQuery);
        List<Slot> slotList = SlotEngine.getSlotList(queryInfo);
        int totalOffset = 0;
        String maskedQuery = rawQuery;
        for (Slot slot: slotList) {
            String slotName = slot.getSlotName();
            String rawValue = slot.getRawValue();
            int start = slot.getStartIndex() + totalOffset;
            int end = slot.getEndIndex() + totalOffset;
            String maskedText = MASKED_SLOT_LEFT_SIDE + "\"slotName\": \"" + slotName + "\"" + MASKED_SLOT_RIGHT_SIDE;
            maskedQuery = maskedQuery.substring(0, start) + maskedText + maskedQuery.substring(end);
            totalOffset += maskedText.length() - rawValue.length();
        }
        return maskedQuery;
    }
}
