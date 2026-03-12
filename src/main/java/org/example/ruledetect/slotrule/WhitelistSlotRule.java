package org.example.ruledetect.slotrule;

import org.example.ruledetect.bean.Keyword;
import org.example.ruledetect.bean.SlotRuleType;
import org.example.ruledetect.bean.Slot;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class WhitelistSlotRule extends AbstractSlotRule {

    private final AcAutomaton acAutomaton;

    public WhitelistSlotRule(Collection<Keyword> keywords, Set<String> domainSet) {
        super(SlotRuleType.WHITELIST, domainSet);
        this.acAutomaton = new AcAutomaton(keywords);
    }

    @Override
    protected List<Slot> predict(String query, List<Slot> existedSlotList) {
        List<Slot> slotList = acAutomaton.parseText(query);
        return slotList;
    }
}
