package org.example.ruledetect;

import org.example.ruledetect.bean.QueryInfo;
import org.example.ruledetect.bean.Slot;
import org.example.utils.ChineseArabicNumConverter;
import org.example.utils.ConstUtils;

import java.util.ArrayList;
import java.util.List;

public class SlotPostprocesser {

    private static final String STR_DOMAIN_SYSTEM = ConstUtils.STR_DOMAIN_SYSTEM;
    private static final String STR_TIME = ConstUtils.STR_TIME;
    private static final String STR_TENANT_LINE = ConstUtils.STR_TENANT_LINE;
    private static final String STR_CONCURRENCY = ConstUtils.STR_CONCURRENCY;
    private static final String STR_TASK_CREATE_TIME_BOUND_START = ConstUtils.STR_TASK_CREATE_TIME_BOUND_START;
    private static final String STR_TASK_CREATE_TIME_BOUND_END = ConstUtils.STR_TASK_CREATE_TIME_BOUND_END;
    private static final String STR_YEAR = ConstUtils.STR_YEAR;
    private static final String STR_MONTH = ConstUtils.STR_MONTH;
    private static final String STR_DAY = ConstUtils.STR_DAY;
    private static final String STR_DATE = ConstUtils.STR_DATE;
    private static final String STR_START_DATE = ConstUtils.STR_START_DATE;
    private static final String STR_END_DATE = ConstUtils.STR_END_DATE;
    private static final String STR_EXPECTED_START_TIME = ConstUtils.STR_EXPECTED_START_TIME;
    private static final String STR_EXPECTED_END_TIME = ConstUtils.STR_EXPECTED_END_TIME;
    private static final String STR_EXPECTED_CONNECTED_CALL_COUNT = ConstUtils.STR_EXPECTED_CONNECTED_CALL_COUNT;
    private static final String STR_LINE_RATIO = ConstUtils.STR_LINE_RATIO;

    private static final String STR_HOUR = ConstUtils.STR_HOUR;
    private static final String STR_MINUTE = ConstUtils.STR_MINUTE;
    private static final String STR_PRODUCT = ConstUtils.STR_PRODUCT;

    public static Slot postprocess(Slot slot) {
        SlotAliasTool.getInstance().normalize(slot);

        String rawValue = slot.getRawValue();
        String normedValue = slot.getNormedValue();
        switch (slot.getSlotName()) {
            case STR_CONCURRENCY:
            case STR_EXPECTED_CONNECTED_CALL_COUNT:
            case STR_LINE_RATIO: {
                if (rawValue.startsWith("0")) {
                    slot = null;
                } else {
                    rawValue = rawValue.replaceAll("[Kk]", "千").replaceAll("[Ww]", "万");
                    long arabicNum = ChineseArabicNumConverter.chineseToArabic(rawValue);
                    normedValue = String.valueOf(arabicNum);
                    slot.setNormedValue(normedValue);
                }
                break;
            }
            case STR_START_DATE:
            case STR_END_DATE:
            case STR_DATE: {
                QueryInfo queryInfo = QueryInfo.createQueryInfo(STR_DOMAIN_SYSTEM, rawValue, rawValue);
                List<Slot> tmpSlotList = SlotRuleDetect.predict(queryInfo);
                String year = tmpSlotList.stream()
                        .filter(x -> STR_YEAR.equals(x.getSlotName()))
                        .map(x -> x.getNormedValue())
                        .findFirst().get();
                String month = tmpSlotList.stream()
                        .filter(x -> STR_MONTH.equals(x.getSlotName()))
                        .map(x -> x.getNormedValue())
                        .findFirst().get();
                String day = tmpSlotList.stream()
                        .filter(x -> STR_DAY.equals(x.getSlotName()))
                        .map(x -> x.getNormedValue())
                        .findFirst().get();
                year = String.format("%04d", Integer.parseInt(year));
                month = String.format("%02d", Integer.parseInt(month));
                day = String.format("%02d", Integer.parseInt(day));
                String strDate = year + "-" + month + "-" + day;
                slot.setNormedValue(strDate);
                break;
            }
            case STR_YEAR:
            case STR_MONTH:
            case STR_DAY: {
                normedValue = String.valueOf(Integer.parseInt(normedValue));
                slot.setNormedValue(normedValue);
                break;
            }
            case STR_TIME:
            case STR_TASK_CREATE_TIME_BOUND_START:
            case STR_TASK_CREATE_TIME_BOUND_END:
            case STR_EXPECTED_START_TIME:
            case STR_EXPECTED_END_TIME: {
                QueryInfo queryInfo = QueryInfo.createQueryInfo(STR_DOMAIN_SYSTEM, rawValue, rawValue);
                List<Slot> tmpSlotList = SlotRuleDetect.predict(queryInfo);
                String hour = tmpSlotList.stream()
                        .filter(x -> STR_HOUR.equals(x.getSlotName())).findFirst().get().getRawValue();
                long intHour = ChineseArabicNumConverter.chineseToArabic(hour);
                if (rawValue.contains("下午")) {
                    intHour += 12;
                }
                hour = String.format("%02d", intHour);

                Slot minuteSlot = tmpSlotList.stream()
                        .filter(x -> STR_MINUTE.equals(x.getSlotName())).findFirst().orElse(null);
                String minute;
                if (minuteSlot == null) {
                    minute = "00";
                } else {
                    minute = minuteSlot.getRawValue();
                    if (minute.endsWith("分")) {
                        minute = minute.substring(0, minute.length() - 1);
                    }
                    if ("一刻".equals(minute)) {
                        minute = "15";
                    } else if ("三刻".equals(minute)) {
                        minute = "45";
                    } else if ("半".equals(minute)) {
                        minute = "30";
                    }
                    minute = String.format("%02d", ChineseArabicNumConverter.chineseToArabic(minute));
                }
                normedValue = hour + ":" + minute;
                slot.setNormedValue(normedValue);
            }
            default:
                break;
        }
        return slot;
    }

    public static List<Slot> postprocess(List<Slot> slotList) {
        List<Slot> tmpSlotList = new ArrayList<>();
        for (Slot slot: slotList) {
            Slot tmpSLot = postprocess(slot);
            if (tmpSLot != null) {
                tmpSlotList.add(tmpSLot);
            }
        }
        return tmpSlotList;
    }
}
