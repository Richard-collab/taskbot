package org.example.chat.bean.baize;

import org.example.utils.JsonUtils;

import java.io.Serializable;

public class AccountProductInfo implements Serializable {

    private String account;
    private String product;
    private String entranceMode;

    public AccountProductInfo(String account, String product, String entranceMode) {
        this.account = account;
        this.product = product;
        this.entranceMode = entranceMode;
    }

    public String getAccount() {
        return account;
    }

    public String getProduct() {
        return product;
    }

    public String getEntranceMode() {
        return entranceMode;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
