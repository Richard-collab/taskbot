package org.example.chat.bean.baize;

import lombok.Getter;

@Getter
public class LinePairConcurrencyInfo {

    private String supplyLineNumber;
    private String tenantLineNumber;
    private int supplyLineConcurrencyInTenantLine; // 为null表示不设置并发限制

    public LinePairConcurrencyInfo(String supplyLineNumber, String tenantLineNumber, int supplyLineConcurrencyInTenantLine) {
        this.supplyLineNumber = supplyLineNumber;
        this.tenantLineNumber = tenantLineNumber;
        this.supplyLineConcurrencyInTenantLine = supplyLineConcurrencyInTenantLine;
    }
}
