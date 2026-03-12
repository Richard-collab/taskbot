package org.example.ruledetect.slotrule;

import org.example.ruledetect.bean.Domain;
import org.example.ruledetect.bean.QueryInfo;
import org.example.ruledetect.bean.SlotRuleType;
import org.example.ruledetect.bean.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractSlotRule {

    private final SlotRuleType ruleType;

    private final Set<String> domainSet;

    public AbstractSlotRule(SlotRuleType ruleType, Set<String> domainSet) {
        this.ruleType = ruleType;
        this.domainSet = domainSet;
    }

    protected abstract List<Slot> predict(String query, List<Slot> existedSlotList);

    public List<Slot> predict(QueryInfo queryInfo, List<Slot> existedSlotList) {
        if (matchDomain(queryInfo.getDomain())) {
            String query = queryInfo.getRawQuery();
            return predict(query, existedSlotList);
        } else {
            return new ArrayList<>();
        }
    }

    public SlotRuleType getRuleType() {
        return ruleType;
    }

    public Set<String> getDomainSet() {
        return domainSet;
    }

    public boolean matchDomain(String domain) {
        return Domain.match(this.getDomainSet(), domain);
    }

}
