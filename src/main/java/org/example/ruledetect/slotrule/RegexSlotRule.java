package org.example.ruledetect.slotrule;

import com.google.common.base.Preconditions;
import org.example.ruledetect.bean.RegexMode;
import org.example.ruledetect.bean.SlotRuleType;
import org.example.ruledetect.bean.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSlotRule extends AbstractSlotRule {

    private final List<String> slotNameList;
    private final String strRule;
    private final List<String> normedValueList;
    private final float score;
    private final Pattern pattern;
    private final String source;

    public RegexSlotRule(
            List<String> slotNameList, RegexMode regexMode, Set<String> domainSet, String strRule, List<String> normedValueList,
            float score, String source) {
        super(SlotRuleType.REGEX, domainSet);
        Preconditions.checkArgument(slotNameList.size() == normedValueList.size());
        this.slotNameList = slotNameList;
        this.strRule = strRule;
        this.normedValueList = normedValueList;
        this.score = score;
        this.pattern = Pattern.compile(strRule, regexMode.getFlag()); // 不传入flags参数，不允许跨行匹配
        this.source = source;
    }

    @Override
    protected List<Slot> predict(String query, List<Slot> existedSlotList) {
        List<Slot> slotList = new ArrayList<>();
        Matcher matcher = pattern.matcher(query);
        while(matcher.find()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            if (matcher.groupCount() == slotNameList.size()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String slotName = slotNameList.get(i - 1);
                    String normedValue = normedValueList.get(i - 1);
                    int start = matcher.start(i);
                    int end = matcher.end(i);
                    String rawValue = query.substring(start, end);
                    if (normedValue == null) {
                        normedValue = rawValue;
                    }
                    String[] curSlotNameArray = slotName.split("\\|", 0);
                    boolean retained = curSlotNameArray.length > 1;
                    for (String curSlotName: curSlotNameArray) {
                        Slot slot = new Slot(curSlotName, rawValue, normedValue, start, end, score, retained, source, source + "_" + uuid);
                        slotList.add(slot);
                    }
                }
            }
        }
        return slotList;
    }

}
