package org.example.chat.bean.baize;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public enum CallbackStatus {

    GET_THROUGH("呼叫成功", ImmutableSet.of("7")),
    NOT_CONNECTED("未接通", ImmutableSet.of("0", "1", "2", "4", "5", "6", "8", "9", "12", "13", "10", "15", "16")),
    TENANT_BLACKLIST("商户黑名单", ImmutableSet.of("11")),
    ;

    private String caption;
    private Set<String> idSet;

    CallbackStatus(String caption, Set<String> idSet) {
        this.caption = caption;
        this.idSet = idSet;
    }

    public String getCaption() {
        return caption;
    }

    public Set<String> getIdSet() {
        return idSet;
    }

    public static CallbackStatus fromCaption(String caption) {
        CallbackStatus result = null;
        for (CallbackStatus callbackStatus: CallbackStatus.values()) {
            if (Objects.equals(callbackStatus.getCaption(), caption)) {
                result = callbackStatus;
                break;
            }
        }
        return result;
    }

    public static Set<String> getValueSet(Collection<CallbackStatus> callbackStatusCollection) {
        return callbackStatusCollection.stream().flatMap(x -> x.getIdSet().stream()).collect(Collectors.toSet());
    }
}
