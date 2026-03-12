package org.example.chat.bean.baize;

import lombok.Getter;
import lombok.Setter;
import org.example.utils.JsonUtils;

import java.io.Serializable;

@Getter
@Setter
public class LoginResult implements Serializable {

    private int id;
    private String account;
    private String roleName;
    private int roleId;
    private String token;
    private int userId;
    private int accountType;
    private int tenantId;
    private String groupId;

    @Override
    public String toString() {
        return JsonUtils.toJson(this, false);
    }
}
