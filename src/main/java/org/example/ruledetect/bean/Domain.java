package org.example.ruledetect.bean;

import java.util.Set;

public class Domain {

    public static final String ALL = "ALL";
    public static final String SHANGHUYUNYING = "商户运营";

    public static boolean match(Set<String> domainSet, String domain) {
        return domainSet.contains(Domain.ALL) || domainSet.contains(domain);
    }
}
