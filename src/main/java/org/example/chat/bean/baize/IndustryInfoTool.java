package org.example.chat.bean.baize;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IndustryInfoTool {

    private final Map<String, Integer> firstIndustryName2industryId;
    private final Map<Integer, String> firstIndustryId2industryName;

    private final Map<String, Integer> secondIndustryName2industryId;
    private final Map<Integer, String> secondIndustryId2industryName;
    private final Map<Integer, Set<Integer>> firstIndustryId2secondIndustryIdSet;
    private final Map<Integer, Integer> secondIndustryId2firstIndustryId;

    public IndustryInfoTool(Map<Integer, String> firstIndustryId2industryName, Map<Integer, String> secondIndustryId2industryName, Map<Integer, Integer> secondIndustryId2firstIndustryId) {
        this.firstIndustryId2industryName = firstIndustryId2industryName;
        this.secondIndustryId2industryName = secondIndustryId2industryName;
        this.secondIndustryId2firstIndustryId = secondIndustryId2firstIndustryId;

        this.firstIndustryName2industryId = firstIndustryId2industryName.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        this.secondIndustryName2industryId = secondIndustryId2industryName.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        this.firstIndustryId2secondIndustryIdSet = secondIndustryId2firstIndustryId.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, x -> Sets.newHashSet(x.getKey()), (oldSet, newSet) -> {
                    Set<Integer> mergedSet = new HashSet<>(oldSet);
                    mergedSet.addAll(newSet);
                    return mergedSet;
                }));

    }

    public Integer getFirstIndustryId(String firstIndustryName) {
        return firstIndustryName2industryId.get(firstIndustryName);
    }

    public Integer getSecondIndustryId(String secondIndustryName) {
        return secondIndustryName2industryId.get(secondIndustryName);
    }

    public String getFirstIndustryName(Integer firstIndustryId) {
        return firstIndustryId2industryName.get(firstIndustryId);
    }

    public String getSecondIndustryName(Integer secondIndustryId) {
        return secondIndustryId2industryName.get(secondIndustryId);
    }

    public Set<Integer> getSecondIndustryIdSet(Integer firstIndustryId) {
        return firstIndustryId2secondIndustryIdSet.get(firstIndustryId);
    }

    public Integer getFirstIndustryId(Integer secondIndustryId) {
        return secondIndustryId2firstIndustryId.get(secondIndustryId);
    }

    public Set<Integer> getFirstIndustryIdSet() {
        return firstIndustryId2secondIndustryIdSet.keySet();
    }

    public Set<Integer> getSecondIndustryIdSet() {
        return secondIndustryId2firstIndustryId.keySet();
    }
}
