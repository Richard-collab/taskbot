package org.example.chat.bean.baize;

import lombok.Getter;
import org.example.utils.JsonUtils;
import org.example.utils.StringUtils;

import java.io.Serializable;

@Getter
public class AccountInfo implements Serializable {

    private String account;
    private String password;
    private String name;
    private Role role;
    private String roleName;

    public AccountInfo(String account, String password, String name, String roleName) {
        if (account == null) {
            account = "";
        } else {
            account = account.trim();
        }
        if (password == null) {
            password = "";
        } else {
            password = password.trim();
        }
        if (name == null) {
            name = "";
        } else {
            name = name.trim();
        }
        if (roleName == null) {
            roleName = "";
        } else {
            roleName = roleName.trim();
        }
        this.account = account;
        this.password = password;
        this.name = name;
        this.roleName = roleName;
        this.role = Role.fromCaption(roleName);
    }

    public boolean checkValid() {
        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(password) || StringUtils.isEmpty(name)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }

}
